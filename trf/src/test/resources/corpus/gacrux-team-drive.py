#!/usr/bin/env python3
"""
Plays random Swiss Team (C.04.6) tournaments with Gacrux @ 6419149 as the pairing engine and writes each as a
TRF26 file whose every round is Gacrux's pairing. By default Gacrux is patched with tpn-order (bracket seats by TPN
only, C.04.6 3.6.1; ruling G1), the 162 states P 0.5 and Baku comes only without a secondary score: the corpus of
the literal readings. With DEFAULTS=1 Gacrux is plain and none of those are avoided (162 has no P, Baku with a
secondary score is played): the corpus of the engine's defaults, the reference app's readings (ADR 0009).

Usage: [DEFAULTS=1] GACRUX=<TieBreakServer clone> python3 gacrux-team-drive.py OUT_DIR COUNT SEED
       (the script re-runs itself as `--pair FILE ROUND` inside Gacrux's Python to pair one round)
"""
import math
import os
import random
import subprocess
import sys

DEFAULTS = os.environ.get("DEFAULTS") == "1"
TYPES = ["FIDE_TEAM_TYPEA_MP_GP", "FIDE_TEAM_TYPEB_MP_GP", "FIDE_TEAM_TYPEA_MP", "FIDE_TEAM_TYPEB_MP",
         "FIDE_TEAM_MP_GP", "FIDE_TEAM_TYPEA_GP_MP", "FIDE_TEAM_TYPEB_GP", "FIDE_TEAM",
         "FIDE_TEAM_TYPEA_MP_BAKU", "FIDE_TEAM_TYPEB_MP_BAKU"]
if DEFAULTS:
    TYPES += ["FIDE_TEAM_TYPEA_MP_GP_BAKU", "FIDE_TEAM_TYPEB_MP_GP_BAKU"]
# Baku only without a secondary score: Gacrux adds the match-point Virtual Points to the game points it uses
# for 4.2.2 (Known Divergence KD-3). 162 states P: without it Gacrux gives the PAB a win per board (KD-2).


def pair_one_round(path, rnd):
    import runpy
    sys.path.insert(0, os.path.join(os.environ["GACRUX"], "gacrux"))
    import crosstablefideteam as ctf
    if not DEFAULTS:
        original = ctf.crosstable_fideteam.update_bracket
        ctf.crosstable_fideteam.update_bracket = (
            lambda self, sl, nodes, edges: original(self, sl, sorted(nodes, key=lambda n: n["tpn"]), edges))
    sys.argv = ["pairingchecker.py", "-i", path, "-p", "-n", str(rnd), "-dT"]
    os.chdir(os.path.join(os.environ["GACRUX"], "gacrux"))
    runpy.run_path("pairingchecker.py", run_name="__main__")


def primary_is_game_points(kind):
    scores = [part for part in kind.split("_") if part in ("MP", "GP")]
    return scores[:1] == ["GP"]


def rank(team, kind, mp, gp):
    """The rank on the primary score alone, ties shared: the standings of a file without 202/212."""
    score = gp if primary_is_game_points(kind) else mp
    return 1 + sum(1 for other in score if score[other] > score[team])


def write(path, name, teams, boards, rounds, kind, top, pattern, history, mp, gp):
    lines = ["012 " + name, "062 %d" % (teams * boards), "072 %d" % (teams * boards), "082 %d" % teams,
             "142 %d" % rounds, "152 %s" % top, "192 " + kind,
             "162  W 1.0    D 0.5    L 0.0" + ("" if DEFAULTS else "    P 0.5"), "362 TW 2.0   TD 1.0   TL 0.0", "352 " + pattern]
    if kind.endswith("_BAKU"):
        lines += baku_records(teams, rounds)
    for t in range(1, teams + 1):
        members = "".join("%5d" % ((t - 1) * boards + b) for b in range(1, boards + 1))
        lines.append("310 %3d %-32s %-5s %6d %6.1f %6.1f %3d %s" % (
            t, "Team %d" % t, "T%d" % t, 2400 - 10 * t, mp[t], gp[t], rank(t, kind, mp, gp), members))
    for t in range(1, teams + 1):
        for b in range(1, boards + 1):
            pid = (t - 1) * boards + b
            line = "001 %4d      %-33s %4d%28s%4.1f%5d" % (pid, "T%dB%d" % (t, b), 2400 - 10 * t, "", 0.0, 0)
            for rnd in history:
                line += "  " + rnd[t][b]
            lines.append(line)
    with open(path, "w") as f:
        f.write("\r\n".join(lines) + "\r\n")


def baku_records(teams, rounds):
    """C.04.7 1.2-1.4 in match points, as explicit 250 records: Gacrux accelerates only from 250."""
    group = 2 * math.ceil(teams / 4)
    accelerated = math.ceil(rounds / 2)
    full = math.ceil(accelerated / 2)
    lines = ["250 %4.1f %4.1f %3d %3d %4d %4d" % (2.0, 0.0, 1, full, 1, group)]
    if accelerated > full:
        lines.append("250 %4.1f %4.1f %3d %3d %4d %4d" % (1.0, 0.0, full + 1, accelerated, 1, group))
    return lines


def play_round(pairs, pab, teams, boards, pattern, rng, mp, gp):
    """One round's blocks per team and board, and the match and game points it scores."""
    rnd = {t: {} for t in range(1, teams + 1)}
    for w, b in pairs:
        no_show = rng.choice([w, b]) if rng.random() < 0.02 else None
        wgp = bgp = 0.0
        for board in range(1, boards + 1):
            (wt, bt) = (w, b) if pattern[board - 1] == pattern[0] else (b, w)
            wp, bp = (wt - 1) * boards + board, (bt - 1) * boards + board
            if no_show is not None or rng.random() < 0.02:
                loser = no_show if no_show is not None else rng.choice([wt, bt])
                wr, br = ("-", "+") if loser == wt else ("+", "-")
            else:
                edge = 0.5 + (bt - wt) / (4.0 * teams)
                x = rng.random()
                wr, br = ("1", "0") if x < edge * 0.7 else ("=", "=") if x < edge * 0.7 + 0.3 else ("0", "1")
            value = {"1": 1.0, "+": 1.0, "=": 0.5, "0": 0.0, "-": 0.0}
            if wt == w:
                wgp += value[wr]
                bgp += value[br]
            else:
                wgp += value[br]
                bgp += value[wr]
            rnd[wt][board] = "%4d w %s" % (bp, wr)
            rnd[bt][board] = "%4d b %s" % (wp, br)
        gp[w] += wgp
        gp[b] += bgp
        if no_show == w:
            mp[b] += 2.0
        elif no_show == b:
            mp[w] += 2.0
        else:
            mp[w] += 2.0 if wgp > bgp else 1.0 if wgp == bgp else 0.0
            mp[b] += 2.0 if bgp > wgp else 1.0 if wgp == bgp else 0.0
    if pab:
        for board in range(1, boards + 1):
            rnd[pab][board] = "0000 - U"
        mp[pab] += 1.0
        gp[pab] += boards * (1.0 if DEFAULTS else 0.5)
    return rnd


def gacrux_pairing(path, rnd):
    result = subprocess.run([sys.executable, os.path.abspath(__file__), "--pair", path, str(rnd)],
                            capture_output=True, text=True)
    lines = [line.split() for line in result.stdout.splitlines() if line.strip()]
    if result.returncode != 0 or not lines or len(lines[0]) != 1:
        return None
    count = int(lines[0][0])
    pairs, pab = [], None
    for w, b in lines[1:count + 1]:
        if b != "0":
            pairs.append((int(w), int(b)))
        elif pab is None:
            pab = int(w)
        else:
            return None  # Gacrux leaves teams unpaired when the round-pairing cannot be completed (3.3.3)
    return pairs, pab


def main():
    out_dir, count, seed = sys.argv[1], int(sys.argv[2]), int(sys.argv[3])
    for n in range(count):
        rng = random.Random(seed + n)
        teams = rng.randint(6, 24)
        boards = rng.choice([2, 3, 4, 4, 5, 6])
        rounds = rng.randint(4, min(11, teams - 2))
        kind = rng.choice(TYPES)
        top = rng.choice("WB")
        pattern = "".join("W" if i % 2 == 0 else "B" for i in range(boards))
        name = "team-%d" % (seed + n)
        path = os.path.join(out_dir, name + ".trf")
        history, mp, gp = [], {t: 0.0 for t in range(1, teams + 1)}, {t: 0.0 for t in range(1, teams + 1)}
        for rnd in range(1, rounds + 1):
            write(path, name, teams, boards, rounds, kind, top, pattern, history, mp, gp)
            got = gacrux_pairing(path, rnd)
            if got is None:
                print(name, "round", rnd, "unpaired", file=sys.stderr)
                break
            history.append(play_round(got[0], got[1], teams, boards, pattern, rng, mp, gp))
        write(path, name, teams, boards, rounds, kind, top, pattern, history, mp, gp)
        print(name, teams, "teams", boards, "boards", len(history), "rounds", kind, file=sys.stderr)


if __name__ == "__main__":
    if sys.argv[1] == "--pair":
        pair_one_round(sys.argv[2], int(sys.argv[3]))
    else:
        main()

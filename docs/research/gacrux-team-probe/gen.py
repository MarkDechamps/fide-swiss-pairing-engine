#!/usr/bin/env python3
"""
Generate the hand-crafted TRF-2026 team tournaments of the Gacrux C.04.6 probe.

Every team has two boards. Team T has players 2T-1 (board 1) and 2T (board 2), and
the board colours follow the default "WBWB" pattern: the white team has White on
board 1 and Black on board 2 (C.04.6 art. 1.6.1: the team colour is the board-1 colour).

A match is (white team, black team, result), result seen from the white team:
  "W"  2-0 over the board        "D"  1-1 (two draws)       "L"  0-2
  "+"  white team wins by forfeit (every board +/-)
  "-"  black team wins by forfeit
  "--" double forfeit (every board -/-)
A round also lists absent teams ("Z": unpaired, zero points) and PAB teams ("U").

Run: python3 gen.py   (writes case*.trf next to this script)
"""
from pathlib import Path

HERE = Path(__file__).parent

GAME = {  # board result for (white-team player, black-team player)
    "W": ("1", "0"), "D": ("=", "="), "L": ("0", "1"),
    "+": ("+", "-"), "-": ("-", "+"), "--": ("-", "-"),
}
GPTS = {"1": 1.0, "=": 0.5, "0": 0.0, "+": 1.0, "-": 0.0}
MP = {"W": (2, 0), "D": (1, 1), "L": (0, 2), "+": (2, 0), "-": (0, 2), "--": (0, 0)}


def build(name, numteams, numrounds, rounds, type192, extra=(), comment=""):
    """rounds: list of dicts {"m": [(w, b, res)], "abs": [teams], "pab": [teams]}"""
    players = {p: [] for p in range(1, 2 * numteams + 1)}   # per round: 10-char entry
    mp = {t: 0.0 for t in range(1, numteams + 1)}
    gp = {t: 0.0 for t in range(1, numteams + 1)}
    for rnd in rounds:
        seen = set()
        for (w, b, res) in rnd.get("m", []):
            wr, br = GAME[res]
            # board 1: white team's player has White
            w1, w2, b1, b2 = 2 * w - 1, 2 * w, 2 * b - 1, 2 * b
            players[w1].append(f"{b1:4} w {wr}")
            players[b1].append(f"{w1:4} b {br}")
            # board 2: black team's player has White
            players[b2].append(f"{w2:4} w {br}")
            players[w2].append(f"{b2:4} b {wr}")
            mw, mb = MP[res]
            mp[w] += mw
            mp[b] += mb
            gp[w] += 2 * GPTS[wr]
            gp[b] += 2 * GPTS[br]
            seen |= {w, b}
        for t in rnd.get("abs", []):
            for p in (2 * t - 1, 2 * t):
                players[p].append("0000 - Z")
            seen.add(t)
        for t in rnd.get("pab", []):
            for p in (2 * t - 1, 2 * t):
                players[p].append("0000 - U")
            mp[t] += 1          # C.04.6 art. 1.4: a PAB is worth a drawn match
            gp[t] += 1.0        # (and the game points of a drawn match: 1 of 2)
            seen.add(t)
        missing = set(range(1, numteams + 1)) - seen
        assert not missing, f"{name}: teams {missing} have no entry in a round"
    lines = [
        f"012 Gacrux C.04.6 probe: {name}",
        f"062 {2 * numteams}",
        f"072 {2 * numteams}",
        f"082 {numteams}",
        f"142 {numrounds}",
        "152 W",
        f"192 {type192}",
        "162  W 1.0    D 0.5    L 0.0    P 0.5",
        "362 TW 2.0   TD 1.0   TL 0.0",
    ]
    for p in range(1, 2 * numteams + 1):
        team = (p + 1) // 2
        board = 2 - p % 2
        pts = sum(GPTS[e[-1]] if e[-1] in GPTS else (0.5 if e[-1] == "U" else 0.0) for e in players[p])
        name_ = f"T{team}B{board}"
        line = f"001 {p:4}      {name_:<33} {2400 - 10 * p:4}{'':28}{pts:4.1f} {p:4}  "
        assert len(line) == 91, len(line)
        line += "  ".join(players[p])
        lines.append(line)
    for t in range(1, numteams + 1):
        tname = f"Team {t}"
        line = f"310 {t:3} {tname:<32}{'':7}{2400 - 20 * t:6} {mp[t]:6.1f} {gp[t]:6.1f} {t:3}  {2 * t - 1:4} {2 * t:4}"
        lines.append(line)
    lines += list(extra)
    text = "\n".join(lines) + "\n"
    (HERE / f"{name}.trf").write_text(text)
    return text


def rr(m=(), abs_=(), pab=()):
    return {"m": list(m), "abs": list(abs_), "pab": list(pab)}


# ---------------------------------------------------------------------------------
# Case 1 - bracket ordering (art. 3.6.1): upfloater 3 has a smaller TPN than resident 4.
build("case1-bracket-order", 8, 5, [
    rr([(1, 5, "W"), (2, 6, "W"), (3, 7, "D"), (4, 8, "W")]),
], "FIDE_TEAM_TYPEA_MP_GP")

# ---------------------------------------------------------------------------------
# Case 2 - [C6] parity (pass/fail) vs graded. t=1, F={2..6} at 3 MP, L={7..10}.
# 3,4,5,6 have all met (all draws); 2 has met only 8, 10, 9. Pair round 4 of 5
# (last two rounds: [C7]/[C10] off, so only [C6] separates the candidate sets).
build("case2-c6-graded", 10, 5, [
    rr([(1, 7, "W"), (2, 8, "D"), (3, 4, "D"), (5, 6, "D")], abs_=[9, 10]),
    rr([(1, 9, "W"), (2, 10, "D"), (3, 5, "D"), (4, 6, "D")], abs_=[7, 8]),
    rr([(1, 10, "W"), (2, 9, "D"), (3, 6, "D"), (4, 5, "D")], abs_=[7, 8]),
], "FIDE_TEAM_TYPEA_MP_GP")

# ---------------------------------------------------------------------------------
# Case 3 - "complies with [C7]" when [C7] cannot reach zero. Pair round 3 of 6.
# T={1,2} (met), F={3,4,5}: 3 and 4 floated in round 2, 5 did not.
build("case3-c7-nonzero", 10, 6, [
    rr([(1, 6, "W"), (2, 7, "W"), (3, 4, "D")], abs_=[5, 10, 8, 9]),
    rr([(1, 2, "D"), (3, 6, "D"), (4, 7, "D"), (5, 10, "W"), (8, 9, "D")]),
], "FIDE_TEAM_TYPEA_MP_GP")

# ---------------------------------------------------------------------------------
# Case 4a - does a forfeited match use up [C1]? Round 1: 1-2 double forfeit.
build("case4a-forfeit-c1", 6, 5, [
    rr([(1, 2, "--"), (3, 4, "D"), (5, 6, "D")]),
], "FIDE_TEAM_TYPEA_MP_GP")

# Case 4b - does a forfeited match count in art. 3.4.3 "most matches played"?
# Round 1: 1 beats 2 over the board, 3 beats 4 by forfeit, 5 gets the PAB.
build("case4b-forfeit-pab", 5, 5, [
    rr([(1, 2, "W"), (3, 4, "+")], pab=[5]),
], "FIDE_TEAM_TYPEA_MP_GP",
    extra=["330 +-   1   3   4"])

# ---------------------------------------------------------------------------------
# Case 5 - Type B, last round, CD 0 with the last two played matches White.
# Rounds 1-4 are four 1-factors of K6, so round 5 is forced: 1-2, 3-6, 4-5.
# Team 1: B B W W (CD 0, last two WW). Team 2: -- W B W (CD +1: mild Black).
build("case5-typeb-lastround", 6, 5, [
    rr([(6, 1, "D"), (2, 5, "--"), (3, 4, "D")]),
    rr([(5, 1, "D"), (6, 4, "D"), (2, 3, "D")]),
    rr([(1, 4, "D"), (5, 3, "D"), (6, 2, "D")]),
    rr([(1, 3, "D"), (2, 4, "D"), (6, 5, "D")]),
], "FIDE_TEAM_TYPEB_MP_GP")

# ---------------------------------------------------------------------------------
# Case 6 - art. 4.3.6: compressed-from-the-end vs round-by-round.
# Team 1: - B W B (absent round 1). Team 2: B W B - (absent round 4). Both CD -1, no
# preference. Team 1 is the first-team (same MP, no secondary score, smaller TPN).
build("case6-433-alignment", 6, 6, [
    rr([(3, 2, "L"), (4, 5, "D")], abs_=[1, 6]),
    rr([(4, 1, "L"), (2, 5, "W"), (3, 6, "D")]),
    rr([(1, 5, "W"), (6, 2, "L"), (3, 4, "D")]),
    rr([(3, 1, "L")], abs_=[2, 4, 5, 6]),
], "FIDE_TEAM_TYPEA_MP")

# ---------------------------------------------------------------------------------
# Case 7 - floater status under acceleration (C.04.7 Baku, GA = TPN 1-4, +2 MP in
# rounds 1-2). In round 2, team 2 (real 0, pairing 2) beat team 5 (real 2, pairing 2).
build("case7-acc-floater", 10, 7, [
    rr([(1, 4, "W"), (3, 2, "W"), (5, 9, "W"), (6, 8, "D"), (7, 10, "D")]),
    rr([(1, 3, "W"), (2, 5, "W"), (4, 10, "D"), (6, 7, "D"), (8, 9, "D")]),
], "FIDE_TEAM_TYPEA_MP_GP_BAKU",
    extra=["250  2.0  0.0   1   2    1    4"])

if __name__ == "__main__":
    for f in sorted(HERE.glob("*.trf")):
        print(f.name)

#!/usr/bin/env python3
"""
Probe driver: run Gacrux's pairingchecker on one TRF file and dump what it decided.

It runs the same code path as `pairingchecker.py -i FILE -c -p -n ROUND [-u ...]`
(check mode + pairing, so the bracket structure is kept), but prints a compact
summary instead of pairingchecker's text report, whose detail printer crashes on a
PAB bracket (helpers.format_pair, KeyError 0).

Usage: GACRUX=/path/to/TieBreakServer python3 probe.py FILE ROUND [extra pairingchecker args]
"""
import os
import sys

gacrux = os.path.join(os.environ.get("GACRUX", "."), "gacrux")
sys.path.insert(0, gacrux)

import pairingchecker as pc  # noqa: E402

trf, rnd, extra = sys.argv[1], sys.argv[2], sys.argv[3:]
sys.argv = ["pairingchecker.py", "-i", trf, "-c", "-p", "-n", rnd] + extra
pch = pc.pairingchecker()
pch.write_output_file = lambda: 0
pch.common_main()

res = pch.chessfile.result["roundpairing"][0]
cmps = {c["cid"]: c for c in res["competitors"] if 0 < c["cid"] < len(res["competitors"]) - 1}
print(f"== Round {res['round']} (Gacrux {pch.origin})")
print(" TPN  score  num  CD  csq       cop  flt  history")
for cid, c in sorted(cmps.items()):
    if not c["rfp"]:
        continue
    hist = " ".join(str(v) for k, v in c["hst"].items() if isinstance(k, int))
    flt = {0: "-", 1: "down", 2: "up"}.get(c["flt"] % 4, c["flt"])
    print(f" {cid:3}  {str(c['acc']):5}  {c['num'].get('val', 0):3}  {c['cod']:2}  {c['csq'].strip():8}  {c['cop']:3}  {flt:4} {hist}")
print()
for b in res["pairing"]:
    if b is None or "pairs" not in b:
        continue
    order = sorted(b.get("bsne", {}).items(), key=lambda kv: kv[1])
    print(f"-- bracket scorelevel {b.get('scorelevel')}{' (PAB)' if b.get('pab') and len(b.get('competitors', [])) == 1 else ''}:"
          f" bsn order {[cid for cid, _ in order]}, upfloaters {b.get('upfloaters')}")
    print(f"   quality {b.get('quality')}")
    for p in b["pairs"]:
        print(f"   {p['w']:3} - {p['b']:3}   (colour rule {p.get('colorrule')})")
print()
print("Pairing (board order, white - black):", ", ".join(f"{w}-{b}" for w, b in res["pairs"]))

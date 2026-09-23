#!/usr/bin/env python3
"""
Counterfactual driver: rerun probe.py with ONE Gacrux decision point replaced by the
competing reading of C.04.6, so the alternative pairing is observed, not only derived.
Gacrux itself is not modified on disk; the patch is applied in memory.

Usage: GACRUX=/path/to/TieBreakServer python3 alt.py PATCH FILE ROUND
  tpn-order    art. 3.6.1: bracket seats by TPN only (not score, then TPN)
  c6-graded    [C6]: prefer the set after which the following scoregroup needs the
               fewest upfloaters (instead of pass/fail at its parity minimum)
  c7-zero      3.5.5: take the first set with [C7] = 0, else the first set (no
               minimisation of a non-zero [C7])
  typeb-none   art. 1.7.2: CD 0 in the last round gives no Type B preference at all
"""
import os
import runpy
import sys

here = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(os.environ.get("GACRUX", "."), "gacrux"))

import crosstablefideteam as ctf  # noqa: E402
import pairingfideteam as pft  # noqa: E402

patch, rest = sys.argv[1], sys.argv[2:]
P = pft.pairing_fideteam

if patch == "tpn-order":
    # Only the seat numbers (bsn) of art. 3.6.1/3.6.2 change. sort_nodes itself must keep
    # its (score, TPN) order: pairing.get_edges relies on the nodes being sorted by
    # descending score (it reads nodes[-1] as the lowest scorelevel).
    orig_ub = ctf.crosstable_fideteam.update_bracket
    ctf.crosstable_fideteam.update_bracket = (
        lambda self, sl, nodes, edges: orig_ub(self, sl, sorted(nodes, key=lambda n: n["tpn"]), edges))

elif patch == "c6-graded":
    def needed(self, scorelevel, restnodes, restedges):
        """Fewest upfloaters with which the following scoregroup can be paired ([C1], [C3])."""
        following = [n for n in restnodes if n["scorelevel"] == scorelevel - 1]
        if not following:
            return 0
        lower = [n for n in restnodes if n["scorelevel"] < scorelevel - 1]
        from itertools import combinations
        for k in range(len(following) % 2, len(lower) + 1, 2):
            for ups in combinations(lower, k):
                nodes = self.sort_nodes(following + list(ups))
                if not self.can_be_paired(nodes, self.get_edges(nodes, restedges)):
                    continue
                cids = [n["cid"] for n in nodes]
                (mn, me) = self.remove_nodes(restnodes, restedges, cids)
                if self.can_be_paired(mn, me):
                    return k
        return 10 ** 6

    orig_check_c6 = P.check_c6
    P._needed = needed

    def select_upfloaters(self, scorelevel, residents, nodes, edges):
        lower = [n for n in nodes if n["scorelevel"] < scorelevel]
        for numup in range(len(residents) % 2, len(lower) + 1, 2):
            for profile in self.list_profiles(lower, numup):
                best = None
                for index, ups in enumerate(self.list_upfloaters(lower, profile)):
                    bn = self.sort_nodes(residents + ups)
                    pairs = self.pair_teams(scorelevel, bn, self.get_edges(bn, edges))
                    if pairs is None:
                        continue
                    (rn, re) = self.remove_nodes(nodes, edges, [n["cid"] for n in bn])
                    if not self.can_be_paired(rn, re):
                        continue
                    key = (self._needed(scorelevel, rn, re), self.count_c7(ups), index)
                    if best is None or key < best[0]:
                        best = (key, ups, pairs, orig_check_c6(self, scorelevel, rn, re))
                if best is not None:
                    print(f"[c6-graded] chosen key (upfloaters needed next, C7, index) = {best[0]}")
                    return (best[1], best[2], best[3])
        raise pft.GacruxNoLegalPairing("no legal pairing")
    P.select_upfloaters = select_upfloaters

elif patch == "c7-zero":
    orig = P.count_c7
    P.count_c7 = lambda self, ups: 0 if orig(self, ups) == 0 else 1   # zero or "not complied"
    # with a binary C7 every non-zero set ties, and the lexicographic index decides

elif patch == "typeb-none":
    orig_cp = ctf.crosstable_fideteam.color_preference

    def color_preference(self, cod, csq):
        if self.typeb and cod == 0 and self.rnd == self.numrounds:
            return "nc"
        return orig_cp(self, cod, csq)
    ctf.crosstable_fideteam.color_preference = color_preference

else:
    sys.exit("unknown patch " + patch)

sys.argv = [os.path.join(here, "probe.py")] + rest
runpy.run_path(os.path.join(here, "probe.py"), run_name="__main__")

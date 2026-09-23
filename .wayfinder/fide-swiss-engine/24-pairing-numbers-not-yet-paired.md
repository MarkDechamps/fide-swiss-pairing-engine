---
title: Pairing numbers of participants not yet paired
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: []
---

## Question

When a round is paired, which participants hold a Pairing Number: everyone registered, or only those already taken into account for pairing (so a registered participant absent from round 1, like a late entry, has no number yet)? The answer decides TPN parity for every initial-colour rule (Dutch 5.2.5, Dubov 5.2.1, Burstein 5.2.1, DSS 4.3.1, TPS 4.3.1) and TPN tie-breaks. It also decides whether the numbering the library emits per round (Shared domain model) can differ from the TRF's start ranks.

## Context

From Exact optimum finder for Dutch brackets: in 8 of 5,364 bbp v6 rounds, the pairs were identical but round-1 colours differed. Every case had a participant absent in round 1 (a requested bye). bbp v6 numbers only "valid" players, those who have taken part in a pairing so far (`tournament.cpp`: `rankIndex = effectivePairingNumber++`), so everyone below the absentee flips parity. The text says TPN (C.04.3 1.1 → GHR 2), and GHR 2.4–2.5 treat late entries as "only taken into account for the pairing of rounds after the first" with provisional TPNs. Public library API already reads late entries as renumbering at any time (ADutch). Evidence: `docs/research/basic-handling-rules.md` §3; the stress/mix corpora on branch `prototype/dutch-optimum-finder` (cases t354, t386, t400, t609, t652, t660, t669, t746, all round 1).

Also from Readable Swiss Team pairing algorithm: Gacrux does the same for C.04.6. It numbers only teams that are present or were paired before (`crosstable.py`, `if rr[i]["rfp"] or rr[i]["rip"]: tpn += 1`), which fixes 4.3.1's parity. Both programs that could serve as Oracles therefore use this effective numbering.

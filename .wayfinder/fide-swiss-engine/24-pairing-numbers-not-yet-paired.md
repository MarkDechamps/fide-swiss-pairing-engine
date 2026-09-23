---
title: Pairing numbers of participants not yet paired
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

When a round is paired, which participants hold a Pairing Number: everyone registered, or only those already taken into account for pairing (so a registered participant absent from round 1, like a late entry, has no number yet)? The answer decides TPN parity for every initial-colour rule (Dutch 5.2.5, Dubov 5.2.1, Burstein 5.2.1, DSS 4.3.1, TPS 4.3.1) and TPN tie-breaks. It also decides whether the numbering the library emits per round (Shared domain model) can differ from the TRF's start ranks.

## Context

From Exact optimum finder for Dutch brackets: in 8 of 5,364 bbp v6 rounds, the pairs were identical but round-1 colours differed. Every case had a participant absent in round 1 (a requested bye). bbp v6 numbers only "valid" players, those who have taken part in a pairing so far (`tournament.cpp`: `rankIndex = effectivePairingNumber++`), so everyone below the absentee flips parity. The text says TPN (C.04.3 1.1 → GHR 2), and GHR 2.4–2.5 treat late entries as "only taken into account for the pairing of rounds after the first" with provisional TPNs. Public library API already reads late entries as renumbering at any time (ADutch). Evidence: `docs/research/basic-handling-rules.md` §3; the stress/mix corpora on branch `prototype/dutch-optimum-finder` (cases t354, t386, t400, t609, t652, t660, t669, t746, all round 1).

Also from Readable Swiss Team pairing algorithm: Gacrux does the same for C.04.6. It numbers only teams that are present or were paired before (`crosstable.py`, `if rr[i]["rfp"] or rr[i]["rip"]: tpn += 1`), which fixes 4.3.1's parity. Both programs that could serve as Oracles therefore use this effective numbering.

## Resolution

Decided in a grilling session; the author accepted the recommended answers.

1. **Who holds a Pairing Number:** in a round, only the **Numbered Participants**. These are the participants taken into account for the pairing of that round or of an earlier one: present and paired, including the one who gets the Pairing-Allocated Bye. A registered participant who has never been taken into account has no Pairing Number yet. Examples are a Late Entry, or someone with a Requested Bye or an absence in every round so far. This is GHR 2.4 read literally: its definition of a Late Entry ("only taken into account for the pairing of rounds after the first") is about being taken into account for pairing, not about the registration date. It is also what both Oracles do: bbp v6 (`effectivePairingNumber++` over valid players) and Gacrux (`rfp or rip`).
2. **A fixed reading, not an Interpretation.** This differs from the precedent in ADR 0003, and there are three reasons. The text (2.4 with 2.5's provisional TPNs) supports this reading better than "number everyone registered". Both Oracles agree on it. The other reading would only change round-1 colours in rare tournaments that no Oracle could check. See ADR 0006.
3. **Once numbered, always numbered.** A participant who withdraws after being taken into account keeps being ranked in the numbering. This matches bbp: validity is sticky. So parity changes only when someone *joins* the numbering. That event is an arrival, and it renumbers at any time, as already decided in Public library API (ADutch's reading). The GHR 2.3 round-4 freeze still applies only to corrections.
4. **Every initial-colour rule uses this numbering.** This covers Dutch 5.2.5, Dubov 5.2.1, Burstein 5.2.1, DSS 4.3.1 and TPS 4.3.1, through the per-round `PairingNumbers` of the Shared domain model. No system-specific hook is needed.
5. **TRF:** the `001` starting rank (or `310` team number) is the file's id and the input ranking order, not a Pairing Number. The `PairingNumbers` the library emits for a round may therefore differ from the start ranks whenever someone registered was not yet taken into account. The Pairing Reply uses file ids (TRF CLI surface), so it is unaffected. `check` replays each round with that round's own numbering.
6. **The TPN tie-break in Standings:** Numbered Participants are ordered by their Pairing Number from the latest paired round. Any never-numbered participants (for example, entered but never taken into account) come after them in the GHR 2.2 ranking order.
7. **Verification:** this removes the 8 round-1 colour misses against bbp v6 (t354, t386, t400, t609, t652, t660, t669, t746). It also makes TPS 4.3.1 parity agree with Gacrux. Neither needs a Known Divergence.

Glossary: Pairing Number sharpened; Numbered Participant added.

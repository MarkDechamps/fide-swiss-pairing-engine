---
title: Historic rule editions
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: []
---

## Question

Does the library support only the 2026 editions of C.04 (in force since 1 Feb 2026), or also earlier editions (e.g. Dutch 2017), so that historic tournament files can be replayed and checked? The editions differ in PAB eligibility, Dutch float rules for unplayed rounds and colour exceptions, and the endorsed reference programs may still implement the older ones, which matters for differential testing. The same question applies to tie-breaks: C.07 2024 vs 2026 (dummy-opponent caps), and the pre-2023 virtual-opponent rules. If older editions are supported, is an edition a first-class concept in the model?

## Context

Surfaced by the research on Basic and General Handling Rules (`research/basic-handling-rules`, open question 5). Also raised by Tie-break regulations (open question 6). TRF file format found that JaVaFo 2.2 (the engine behind 9 of the 11 accepted Dutch programs) reads only TRF16 and may still implement the 2017 Dutch rules, which bears directly on whether older editions are needed for differential testing. Dutch System research confirmed that JaVaFo 2.2 implements the 2017 rules, and that JaVaFo and bbpPairings v5 agree on 461/461 rounds under 2017 rules, whereas 2026 has only one oracle (bbp v6). A 2017 mode would give a doubly-confirmed conformance baseline.

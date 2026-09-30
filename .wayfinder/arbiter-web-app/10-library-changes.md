---
title: Library changes the web app needs
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: [02]
---

## Question

Which of the ten library changes proposed in docs/research/library-api-fit.md does the library take on, in what shape, and in which release, before the web app build starts? In particular: a `TournamentChange` log with `apply`/`replay` versus the app replaying through today's methods; manual edits on `ProposedPairing`; guarded settings changes on a running tournament; `asBefore(round)` for the correction warning; the TRF round trip (ranking order, late entries, player and event details); first-class collections alongside the raw `List`/`Map` accessors. Each change is weighed against ADR 0008 (SemVer) and the library's small, intention-revealing API.

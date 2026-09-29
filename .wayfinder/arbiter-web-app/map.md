---
title: Arbiter web app
labels: [wayfinder:map]
status: open
---

## Destination

An implementation-ready spec for an **arbiter web app** that runs a real individual Swiss tournament from start to finish on top of the fide-swiss-pairing-engine library: create the tournament, register players, pair rounds, enter results, handle corrections, show standings and export TRF. Spring Boot, Thymeleaf and htmx, in its own `webapp/` folder, ready to hand to a build effort.

## Notes

- Settled while charting (2026-09-29): the app is an **arbiter tool** that runs real tournaments, not a playground. It runs **locally for one user** first (`java -jar` on the arbiter's laptop, works offline, no login), built so that hosting and accounts can be added later by swapping adapters. **Individual systems** come first (every individual system via profiles); teams stay in scope but are fog. It is a **separate Maven build** in `webapp/` with Spring Boot as its parent, depending on the library's published version (SNAPSHOT via `mvn install` while developing). The library's reactor, release and CI stay untouched.
- The user accepted the recommended answers for Q3–Q5 while away from keyboard. Revisit them if a ticket shows they don't hold.
- Stack: Java 25, Spring Boot, Thymeleaf, htmx. Clean code, DDD, clean architecture: a pure domain core, ports and adapters, aggregates and value objects in the ubiquitous language.
- The app is an **outside client** of the library: it uses only the library's public API. A library gap becomes a ticket on the library, never a workaround in the app.
- Skills every session should consult: `grilling`, `domain-modeling`, `clean-java`, `clean-code`, `codebase-design`, `tdd`.
- Glossary: the library's `CONTEXT.md`. The app is likely its own bounded context (see its ticket); keep glossaries current with `domain-modeling`.
- Credit budget: one agent at a time, the cheapest model that fits the job.
- Research findings go in `docs/research/<name>.md`.

## Decisions so far

## Not yet specified

- **Team tournaments**: team rosters, board lineups per round, board results rolling up into match points, and Olympiad specifics. In scope after the individual flow works.
- **Hosting**: accounts and authentication, several arbiters and tournaments per server, public read-only pages for players (pairings, standings), deployment target.
- **Printing and publishing**: pairing sheets, result slips, wall charts, crosstables (HTML print vs PDF).
- **Reporting to FIDE and federations**: rating report submission beyond a TRF export.
- **App CI and releases**: workflow, versioning against library versions, distribution to arbiters.
- **Internationalisation**: UI languages, and whether the domain terms need translation.

## Out of scope

- Rating calculation, non-Swiss formats (round-robin, knockout), and electronic board or clock integration: beyond an arbiter tool for running Swiss tournaments.

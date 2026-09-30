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
- **Encapsulation is key**: objects hide state and expose behaviour (tell, don't ask). **TDD** always. **First-class collections** (Object Calisthenics) for every domain collection.
- Skills every session should consult: `grilling`, `domain-modeling`, `clean-java`, `clean-code`, `codebase-design`, `tdd`, `object-calisthenics`.
- Glossary: the library's `CONTEXT.md`. The app is likely its own bounded context (see its ticket); keep glossaries current with `domain-modeling`.
- Credit budget: one agent at a time, the cheapest model that fits the job.
- Research findings go in `docs/research/<name>.md`.

## Decisions so far

- [Arbiter workflow and tournament lifecycle](01-arbiter-workflow.md): Preparing / Running / Finished (system, edition, scoring and acceleration freeze at round 1); pair, adjust (checked; illegal only as a logged Override), publish, enter results, round complete; unpublish only before any result; GHR 4.3 corrections any time, with later pairings left standing; withdrawals, requested byes and late entries through the library; no general undo, a Tournament Log instead.
- [Current Spring Boot, Thymeleaf and htmx stack on Java 25](05-webapp-stack.md): Boot 4.1.1 + Thymeleaf 3.1.5 + htmx-spring-boot 5.1.0 on Java 25; htmx 2.0.11 vendored (htmx 4 later); library on the classpath with ArchUnit guarding its public API; fragments + `FragmentsRendering` OOB swaps; tests are `@WebMvcTest`, HtmlUnit for no-JS, Playwright in a profile.
- [Library API fit for the arbiter workflow](09-library-api-fit.md): most steps are covered (pairing, `check`, recording an illegal pairing, results, GHR 4.3 corrections, withdraw/bye/late entry, explanations, progress); ten additive changes proposed, top three: a replayable `TournamentChange` log, manual edits on `ProposedPairing`, guarded settings changes while Running; a published but unfinished round stays app state.
- [App domain model and its bounded context](02-app-domain-model.md): its own Tournament Administration context, conformist to the library (library types used directly, no anti-corruption layer or pairing port); one `Tournament` aggregate that encapsulates the library snapshot; the Tournament Log of Arbiter Actions is the single source of truth, replayed into the library (the replayable change log becomes a must-have); proposed pairings are stored, never recomputed.

## Not yet specified

- **Team tournaments**: team rosters, board lineups per round, board results rolling up into match points, and Olympiad specifics. In scope after the individual flow works.
- **Hosting**: accounts and authentication, several arbiters and tournaments per server, public read-only pages for players (pairings, standings), deployment target.
- **Printing and publishing**: pairing sheets, result slips, wall charts, crosstables (HTML print vs PDF).
- **Reporting to FIDE and federations**: rating report submission beyond a TRF export.
- **App CI and releases**: workflow, versioning against library versions, distribution to arbiters.
- **Internationalisation**: UI languages, and whether the domain terms need translation.

## Out of scope

- Rating calculation, non-Swiss formats (round-robin, knockout), and electronic board or clock integration: beyond an arbiter tool for running Swiss tournaments.

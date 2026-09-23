---
title: Project foundation
labels: [wayfinder:grilling]
status: closed
assignee: mark
blocked_by: [02-dutch-system]
---

## Question

What are the project's foundations: Java version, build tool, licence (compatible with how we'll use the reference programs as oracles), module/package layout for library core vs CLI vs per-system code, and repository conventions?

## Resolution

Grilled with the author on 2026-09-23.

- **Java 25** (Temurin), pinned through `.sdkmanrc` (`java=25-tem`).
- **Maven with a committed wrapper** (`mvnw`), the build the pre-commit hook already prefers.
- **Apache-2.0** (see [ADR 0001](../../docs/adr/0001-apache-2-licence.md)). Oracles run only as external processes; JaVaFo output is never committed; Javadoc cites Handbook articles and never reproduces the Handbook.
- **Names**: project `fide-swiss-pairing-engine`; groupId `io.github.markdechamps`; artifacts `fide-swiss-pairing-engine-{core,trf,cli}` plus the unpublished `-oracle-it`; base package `io.github.markdechamps.fideswiss` (e.g. `...fideswiss.dutch`). The README states the project is not affiliated with or endorsed by FIDE.
- **Modules** (Maven multi-module, parent pom):
  - `core`: domain model, Basic and General Handling Rules, every pairing system as its own package, acceleration, tie-breaks. **Zero runtime dependencies.**
  - `trf`: TRF16/TRF26/JaVaFo-dialect reader and writer into the core model.
  - `cli`: JaVaFo/bbp-compatible CLI (pair, `-check`, generator); depends on core and trf.
  - `oracle-it`: comparison runs against bbpPairings, Gacrux and JaVaFo; not published. What goes into it belongs to Verification strategy.
- **A `module-info.java` in every module**; core exports only its API packages, so the algorithm internals stay unexported.
- **Tests**: JUnit 6 (Jupiter) and AssertJ, no Mockito. Handwritten fakes, Mother objects (e.g. `TournamentMother`) and domain assertion helpers. Each Handbook example becomes a test that cites its article, so rule coverage can be traced. Surefire runs unit tests (pre-commit and PR gate); Failsafe runs `*IT` in `oracle-it`, bound to `verify`.
- **Quality**: Spotless with palantir-java-format enforced in `verify`; `-Xlint:all -Werror`; a JaCoCo report with no threshold; PIT mutation testing on core in the nightly workflow. No Checkstyle, SpotBugs or Error Prone unless a real need shows up.
- **Hosting**: GitHub `MarkDechamps/fide-swiss-pairing-engine`, trunk-based on `main` with short-lived branches. GitHub Actions runs a PR gate (`./mvnw verify` on Java 25) and a nightly or on-demand workflow for the oracle runs (pinned bbpPairings v6 and Gacrux, JaVaFo downloaded at runtime) and PIT.
- **Repository**: this repo is the project repo. `CONTEXT.md` stays at the root, ADRs go in `docs/adr/`. The wayfinder map and the research go public; the research files are to be merged into `main` under `docs/research/` (a build hand-off step, not done yet).
- **Clean-up done in this session** before anything was pushed: history was rewritten with git-filter-repo on every branch to remove files copied from other projects, and `.claude/settings.local.json` is now gitignored. Every commit hash changed, and the research-commit hashes cited in the tickets were remapped.
- Not done (execution for the build): add the GitHub remote and push, merge the research files into `main`, scaffold the poms.

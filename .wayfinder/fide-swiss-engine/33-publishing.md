---
title: Publishing to Maven Central and packaging the CLI
labels: [wayfinder:grilling]
status: closed
assignee: markdechamps
blocked_by: []
---

## Question

How does the library get released: the version scheme (SemVer, and how a new FIDE edition or a changed reading maps onto major/minor), the release process (tagging, GitHub Actions, changelog), signing and publishing to Maven Central under `io.github.markdechamps:fide-swiss-pairing-engine-*`, and how the `fide-swiss` executable ships (fat jar, jlink image, native image, which platforms)? Decide each so the build effort can set it up once.

## Context

Graduated from the "Publishing" fog when the frontier emptied (Double-Swiss matches in TRF). Evidence: Project foundation (coordinates, modules with module-info, Java 25, Maven wrapper, GitHub Actions), TRF CLI surface (the `fide-swiss` command and its exit codes), Verification strategy (release gate: 50k tournaments), Historic rule editions (editions are explicit settings).

## Resolution

Decided on 2026-09-25. The author asked this session to take the recommended answers and to stop only on real blockers. There were none, so each decision below is the recommended one, for the author to overturn. Evidence: Project foundation (coordinates, modules with `module-info`, zero-dependency `core`, GitHub Actions, the current `pom.xml` and workflows), Random tournament generator (the published `generator` module), TRF CLI surface (`fide-swiss`, the launcher over `...fideswiss.cli.Main`, the exit codes as a public contract), Verification strategy (the per-system 50k release gate and definition of done), Historic rule editions and Tournament profiles (editions and defaults are explicit settings). The versioning rule is recorded in [ADR 0008](../../docs/adr/0008-a-pairing-change-is-a-versioned-change.md).

**Version scheme.** SemVer, and it covers behaviour as well as the API (ADR 0008):

- **Public contract**: the exported packages of every published module, the CLI grammar, the Pairing Reply and the exit codes (ADR 0005), the TRF records we read and write, and the output under unchanged settings.
- **Major**: an API or CLI break; a changed default (a Profile's edition, Interpretation or other setting); a fixed reading replaced by another; an edition, system or Interpretation removed.
- **Minor**: anything opt-in: a new system, a new edition (a new FIDE edition lands here and becomes a Profile default only in the next major), a new Interpretation value, a new tie-break, a new CLI flag or subcommand.
- **Patch**: a bug fix, meaning the old output broke a reading the library already documents, shown by a failing test that cites the article. It may change pairings; that is listed under Pairing changes.
- **0.x**: releases go to Maven Central before 1.0, with the usual 0.x rule that a minor may break. **1.0** is the first release in which the Dutch System (2026) meets its definition of done (the 50k release gate against bbp v6), with `trf`, `cli` and `generator` able to run it. Each further system joins in a minor release when it meets its own definition of done; the README status says which.
- All modules share **one version** (the parent's); they are released together.

**Changelog.** A hand-kept `CHANGELOG.md` in the *Keep a Changelog* format, with an `Unreleased` section updated in the same change as the code (like the README). Beside Added/Changed/Fixed/Removed it has a mandatory **Pairing changes** section: every change that can alter a pairing, standing or check verdict, with the article, the systems and editions affected, and whether a Known Divergence was added or closed. The GitHub Release notes are that version's section, copied by the release workflow.

**Release process.** A `release.yml` GitHub Actions workflow, started by hand (`workflow_dispatch` with the version as input) from `main`; no maven-release-plugin.

1. **Gate**: `./mvnw verify` plus the 50k release gate for every system with an Oracle that the README marks as done, run on the exact commit and sharded over a job matrix to fit the runner's 6-hour limit. Systems without an Oracle need their Witness diff reviewed (Verification strategy); the workflow checks that the Known Divergence register has no unreviewed entries.
2. **Version**: `versions:set` to the release version, move `Unreleased` in `CHANGELOG.md` under the new version and date, commit `Release X.Y.Z`, tag `vX.Y.Z`.
3. **Publish**: `./mvnw -Prelease deploy` to Maven Central (below), then a GitHub Release on the tag with the changelog section and the CLI distributions attached.
4. **Next**: `versions:set` to the next minor `-SNAPSHOT`, commit, push `main` and the tag.

A release is only ever cut from a commit that passed step 1, which is why the tag is made by the workflow and not pushed by hand.

**Maven Central.**

- Through the **Sonatype Central Portal** with `org.sonatype.central:central-publishing-maven-plugin` (`autoPublish` on, `waitUntil` published). OSSRH is gone, so there is no staging-repository plugin.
- **Namespace** `io.github.markdechamps`, verified on the Portal through the author's GitHub account. That verification, the Portal user token and the signing key are a one-time HITL checklist for the build effort, not a decision.
- **Published**: the parent pom and `core`, `trf`, `generator`, `cli`. `oracle-it` is skipped (`maven.deploy.skip` plus the plugin's `excludeArtifacts`).
- **Signing**: one project GPG key (published to keys.openpgp.org), held as GitHub secrets (private key, passphrase) and used by `maven-gpg-plugin` with the Bouncy Castle signer, so no gpg agent runs in CI.
- A **`release` profile** holds `maven-source-plugin`, `maven-javadoc-plugin`, the GPG plugin and the Central plugin, so the everyday build stays fast. The pom gains the `developers` block Central requires.
- **Javadoc** is published as the javadoc jar and read on javadoc.io; there is no separate site.
- **Reproducible builds**: `project.build.outputTimestamp` is set, and `versions:set` updates it, so anyone can rebuild a release byte for byte. It matters for a reference library whose outputs are compared.
- **Module version**: the jars carry their module version, and `fide-swiss version` and the library read it from there. The build is the only source of the version.

**Packaging the `fide-swiss` executable.** Two distributions, both attached to the GitHub Release with SHA-256 checksums:

- **Portable zip/tar.gz** (`fide-swiss-X.Y.Z.zip`): `lib/` with the four modular jars, `bin/fide-swiss` and `bin/fide-swiss.bat` running `java -p lib -m io.github.markdechamps.fideswiss.cli/io.github.markdechamps.fideswiss.cli.Main`, the LICENSE and README. It needs Java 25 on the path, and runs on any platform. Built by `maven-assembly-plugin` in `cli`.
- **jlink images** with a trimmed runtime, no Java needed: linux-x64, linux-aarch64, macos-aarch64 and windows-x64, built on a runner of each platform in the release matrix (jlink cannot cross-build). This is trivial because every module has a `module-info` and nothing has a runtime dependency. `jlink --launcher fide-swiss=...` gives the executable, plus `--strip-debug --no-header-files --no-man-pages`.
- **No fat jar**: it would drop the module descriptors that keep `core`'s internals unexported, and the zip already covers `java` users.
- **Native image: not for 1.0.** It would help harnesses that start a process per round, but it adds a GraalVM toolchain per platform. The code stays reflection-free so it can be added later in a minor release.
- **Not for 1.0**: Homebrew, SDKMAN, winget, macOS notarisation and Windows code signing. The macOS image is run from a terminal after removing the quarantine attribute, which the README states.

**Definition of done of the build.** The spec's done includes: the `release` profile and `release.yml`, the `CHANGELOG.md` with its Pairing changes section, and the README naming the Maven coordinates, the distributions and the Java requirement, kept current per the Functional README note.

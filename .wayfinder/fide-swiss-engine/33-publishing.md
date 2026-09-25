---
title: Publishing to Maven Central and packaging the CLI
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: []
---

## Question

How does the library get released: the version scheme (SemVer, and how a new FIDE edition or a changed reading maps onto major/minor), the release process (tagging, GitHub Actions, changelog), signing and publishing to Maven Central under `io.github.markdechamps:fide-swiss-pairing-engine-*`, and how the `fide-swiss` executable ships (fat jar, jlink image, native image, which platforms)? Decide each so the build effort can set it up once.

## Context

Graduated from the "Publishing" fog when the frontier emptied (Double-Swiss matches in TRF). Evidence: Project foundation (coordinates, modules with module-info, Java 25, Maven wrapper, GitHub Actions), TRF CLI surface (the `fide-swiss` command and its exit codes), Verification strategy (release gate: 50k tournaments), Historic rule editions (editions are explicit settings).

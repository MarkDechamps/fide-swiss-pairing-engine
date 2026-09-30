---
title: Architecture, packages and test strategy
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: [02,05]
---

## Question

How is the webapp laid out in clean architecture? One Maven module with packages enforced by ArchUnit, or several modules (domain, application, adapters)? Where do the ports sit (repository, clock, TRF files; the library is a direct domain dependency, per the domain model ticket), how is the library's public API enforced (ArchUnit), and what does the test pyramid look like (TDD domain tests without Spring, slice tests for web adapters, a few end-to-end browser tests)?

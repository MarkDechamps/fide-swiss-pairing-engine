---
title: Public library API
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: []
---

## Question

What are the exact public types and operations of the library, given the immutable `Tournament` snapshot from Shared domain model across all systems? Covers how a client builds a snapshot (participants, settings, completed rounds), the pre-round operations (`withdraw`, `requestBye`, `enterLate`) and result corrections (GHR 4.3), the error model (which exceptions, and what they report), and the exact shape of `RoundPairing` (including the pairing trace) and `Standings`. The aim is the smallest intention-revealing surface that still makes everything FIDE defines possible.

## Context

Graduated from the "Public library API" fog once the domain model was settled.

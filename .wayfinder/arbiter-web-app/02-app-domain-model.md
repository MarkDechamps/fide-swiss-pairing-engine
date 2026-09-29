---
title: App domain model and its bounded context
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: [01]
---

## Question

How does the app's domain relate to the library's? Is the app its own bounded context with its own glossary (CONTEXT-MAP.md), and what are its aggregates and value objects (event, registration, round, result sheet)? Does the app store the recorded facts and rebuild the library's immutable `Tournament` from them each time (event-sourced style), or store a snapshot? Where is the anti-corruption layer between app terms and library terms?

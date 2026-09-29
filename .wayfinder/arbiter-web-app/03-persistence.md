---
title: Persistence for a local single-user app
labels: [wayfinder:grilling]
status: open
assignee:
blocked_by: [02,05]
---

## Question

How are tournaments stored locally so that a hosted database can replace the store later? Covers the embedded database (H2, SQLite, or plain files, possibly TRF itself), JPA vs plain JDBC, schema migrations (Flyway or Liquibase), the data folder location, backup and restore, and crash safety during result entry.

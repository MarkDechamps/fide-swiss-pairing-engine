---
name: test
description: Use when writing tests,guidance assertions, and test philosophy.
---

# Test

Use this skill for all repository-specific unit testing guidance.

## Core principles

- work TDD when possible to drive design and ensure testability.
- When you need to make a method public for testing, ask if you can test the behavior through a public API instead. If not, consider if the method should be package-private and tested from a same-package test class.
- Keep tests fast, deterministic, and focused on observable behavior.
- Prefer behavior assertions over interaction-heavy assertions.
- Prefer handwritten fakes over mocks for in-repo components. A lot of TestDoubles are available already (like eu.adapter.persistence.rentalbreak.RentalBreakRepositoryTestDouble for instance). Use them.
- Use mocks only for external systems or boundaries you do not control.
- Keep unit tests free of Spring context startup.
- Wire dependencies manually with constructor injection.
- Keep any test object-graph helper deterministic.
- Use fixed clocks for time-dependent behavior.
- Fix tests with proper test setup, not production-code workarounds.

## Unit test structure

- Organize related scenarios with `Given` / `When` / `Then`.
- Use `@Nested` to group related behaviors.
- Keep test methods focused on a single behavior.
- Prefer clear, descriptive test names.
- Make tests independent and idempotent.

## Assertions

- Prefer AssertJ for fluent assertions.
- Use `assertThat(...)` style checks when possible.
- Use in-repo assertion helpers for domain-specific checks.
- Assert observable state rather than interactions.

## Test setup

- Prefer fakes and factories over mocks for in-repo components.
- Keep setup deterministic and minimal.
- Follow the project's own convention for separating unit, integration and acceptance tests; confirm it against the repo rather than assuming one.

## Reminders

- Consult existing test helpers before adding new ones.
- Use the repository conventions in `java-junit` skill for general JUnit guidance.

## Mother objects

- Prefer mothers object pattern over using builders when setting up test objects.
- Provide initializer methods to initialize fields in the mother objects that hide the unimportant details but specify (overwrite) in the test the settings that matter. This makes tests shorter and more explicit.




---
title: Current Spring Boot, Thymeleaf and htmx stack on Java 25
labels: [wayfinder:research]
status: closed
assignee: claude
blocked_by: []
---

## Question

What are the current stable versions of Spring Boot, Thymeleaf and htmx, and how well do they work with Java 25 and a library shipped as JPMS modules (module-info)? What are the established patterns for server-rendered htmx with Thymeleaf (fragments, the htmx-spring-boot integration, layout dialect, form validation, out-of-band swaps, progressive enhancement), and how are they tested (Spring MVC slice tests, HtmlUnit, Playwright for Java)?

## Resolution

Boot 4.1.1 (Java 17 to 26, so Java 25 is fine), Thymeleaf 3.1.5 (BOM-managed), htmx-spring-boot 5.1.0. Start on htmx 2.0.11: htmx 4.0.0 is GA but npm `latest` stays 2.x until early 2027, the Spring integration predates it and HtmlUnit has no `fetch()`; keep templates migration-friendly. Run the library's JPMS jars on the classpath (Spring jars are automatic modules, so no `module-info` in the app) and enforce public-API-only with ArchUnit or a small modular domain module. Test with MockMvc slices, HtmlUnit for no-JS flows and Playwright for htmx journeys. Details and sources: `docs/research/webapp-stack.md` on branch `research/webapp-stack`.

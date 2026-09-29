# Arbiter web app stack: Spring Boot, Thymeleaf, htmx on Java 25

Research ticket: `.wayfinder/arbiter-web-app/05-webapp-stack.md`.
Sources retrieved 2026-09-29 (Maven Central metadata, GitHub releases and tags, official docs). Items I could not confirm are marked **[unverified]**.

## Summary and recommendations

1. **Spring Boot 4.1.1** (GA 2026-08-20). Boot 4.2.0-M2 is out (2026-09-24) but is a milestone; the 4.0.x and 3.5.x lines are still patched. Boot 4.1.1 supports **Java 17 to 26**, so Java 25 is fully supported. It brings Spring Framework 7.0.9, Tomcat 11, Jakarta EE 11 (Servlet 6.1), Jackson 3, Hibernate Validator 9.1.
2. **Thymeleaf 3.1.5.RELEASE** (2026-04-21) is what the Boot 4.1.1 BOM manages, through `spring-boot-starter-thymeleaf` and `thymeleaf-spring6`. Thymeleaf has no 4.x. Take it from the BOM and do not pin it.
3. **htmx: start on 2.0.11, design for a cheap move to 4.0.0.** htmx 4.0.0 went GA on 2026-08-28, but the htmx team keeps npm `latest` on 2.x until early 2027 and says 2.x is "supported indefinitely". `htmx-spring-boot` 5.1.0 (March 2026) predates htmx 4 and its README says nothing about it. HtmlUnit has no native `fetch()`, which htmx 4 requires. Vendor the script into `static/` (the app runs offline; no CDN).
4. **Integration library:** add `io.github.wimdeblauwe:htmx-spring-boot-thymeleaf:5.1.0` (needs Boot 4.0.3+, Java 17+). Use `@HxRequest`, `HtmxResponse`, `FragmentsRendering` for out-of-band swaps. The Thymeleaf layout dialect (4.0.1, BOM-managed) is optional; plain parameterised fragments are enough.
5. **JPMS:** run the app on the **classpath** (Boot fat jar, no `module-info.java` in the app). The library's `module-info.class` is then ignored, so its `exports` are not enforced at runtime. Boot and Spring jars are automatic modules only, so the module path is not a supported route. Enforce "public API only" with a build check (ArchUnit or a `maven-enforcer` rule) or by compiling a pure-Java `domain`/`application` Maven module with its own `module-info` (**[unverified]**, see section 3).
6. **Testing:** `@WebMvcTest` + MockMvc for controller and fragment slices (Boot 4 moved it to `spring-boot-starter-webmvc-test`); HtmlUnit through MockMvc for static HTML flows and progressive enhancement without JavaScript; **Playwright for Java 1.63.0** for the few end-to-end htmx journeys (needs a browser download, so gate it behind a Maven profile).

## 1. Versions (as of 2026-09-29)

| Component | Current stable | Notes | Source |
|---|---|---|---|
| Spring Boot | 4.1.1 (2026-08-20) | 4.2.0-M2 (2026-09-24) is a milestone. Also 4.0.8 (2026-08-21), 3.5.16 (2026-06-25). | https://github.com/spring-projects/spring-boot/releases ; https://spring.io/projects/spring-boot ; https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-starter-parent/maven-metadata.xml |
| Spring Framework | 7.0.9 | Required by Boot 4.1.1. | https://docs.spring.io/spring-boot/system-requirements.html |
| Thymeleaf | 3.1.5.RELEASE (2026-04-21) | Managed by Boot BOM, with `thymeleaf-spring6`. | https://repo1.maven.org/maven2/org/thymeleaf/thymeleaf/maven-metadata.xml ; https://repo1.maven.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom |
| Thymeleaf Layout Dialect | 4.0.1 (2026-03) | Managed by Boot BOM. Depends on Thymeleaf 3.1.3. | same BOM ; https://repo1.maven.org/maven2/nz/net/ultraq/thymeleaf/thymeleaf-layout-dialect/maven-metadata.xml |
| htmx | 4.0.0 (GA 2026-08-28); 2.0.11 is npm `latest` | npm dist-tags: `latest=2.0.11`, `next=4.0.0`. | https://github.com/bigskysoftware/htmx/releases ; https://registry.npmjs.org/htmx.org |
| htmx-spring-boot | 5.1.0 (2026-03-14) | Boot 4.0.3+, Java 17. The 4.0.x line is for Boot 3.4/3.5. | https://github.com/wimdeblauwe/htmx-spring-boot |
| HtmlUnit | 5.5.0 (2026-08-30), JDK 17; Boot 4.1.1 BOM manages 4.21.0 | 5.0.0 (2026-05-24) is a major bump. | https://htmlunit.org/changes-report.html ; BOM above |
| Playwright for Java | 1.63.0 (2026-09-14) | Not managed by the Boot BOM. | https://repo1.maven.org/maven2/com/microsoft/playwright/playwright/maven-metadata.xml |

Boot support policy: minor versions get at least 12 months of OSS support, so 4.1 lasts to about mid-2027 at least (https://github.com/spring-projects/spring-boot/wiki/Supported-Versions). The exact end dates are on https://spring.io/projects/spring-boot#support, which I could not read. **[unverified]**

Boot 4 changes that matter for a new app (https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Migration-Guide):
- Modular starters, for example `spring-boot-starter-webmvc`, `spring-boot-starter-thymeleaf`. Each has a `-test` companion such as `spring-boot-starter-webmvc-test`.
- `@SpringBootTest` no longer provides MockMvc on its own. Add `@AutoConfigureMockMvc`.
- `@MockBean` and `@SpyBean` are removed. Use `@MockitoBean` and `@MockitoSpyBean`.
- Jackson 3 (`tools.jackson`) and JSpecify nullability.

## 2. Java 25

Boot 4.1.1 states "Java 17 minimum, Java 26 maximum (inclusive)"; Maven 3.6.3 or later; Tomcat 11 (https://docs.spring.io/spring-boot/system-requirements.html). The Boot and Spring jars are built for Java 17 (`Build-Jdk-Spec: 17` in the manifests I inspected), so they run on 25. The library already compiles with `maven.compiler.release=25`. Nothing further is needed. The Boot GraalVM native-image docs name GraalVM 25, which is irrelevant to a `java -jar` app.

## 3. Consuming a library shipped as JPMS modules

What I checked directly on Maven Central (Boot 4.1.1 / Spring 7.0.9 jars):
- `spring-webmvc-7.0.9.jar`, `spring-boot-thymeleaf-4.1.1.jar`, `spring-boot-webmvc-test-4.1.1.jar` and `thymeleaf-spring6-3.1.5.RELEASE.jar` contain **no `module-info.class`**, only `Automatic-Module-Name` in the manifest (for example `spring.webmvc`, `thymeleaf.spring6`).
- Spring Framework's own request "Declare Spring modules with JDK 9 module metadata" is still open after years (https://github.com/spring-projects/spring-framework/issues/18079).
- Spring's reflection-heavy model (bean access, CGLIB proxies) needs `opens` in module setups, and there is a trail of module-path bugs (https://github.com/spring-projects/spring-framework/issues/32671 ; https://github.com/spring-projects/spring-boot/issues/15967 ; https://github.com/spring-projects/spring-boot/issues/41203). Boot's executable jar launches on the classpath. I found no Boot doc that promises module-path support. **[unverified: I found no positive or negative official statement]**

Consequences for the app:
- **Put the library jars on the classpath.** A modular jar on the classpath works as a normal jar. Its `module-info.class` is ignored and encapsulation is lost, so the app could reach non-exported packages. Enforce the "outside client of the public API" rule with a test (ArchUnit) or an enforcer rule that limits imports to the three exported packages (`tournament`, `pairing`, `standings`, see `core/src/main/java/module-info.java`).
- **Do not add `module-info.java` to the Spring Boot module.** Spring and Thymeleaf are automatic modules, so `requires` needs fragile names and many `opens`.
- Optional idea: keep a pure-Java Maven module (domain and application layers, no Spring) with its own `module-info` that `requires io.github.markdechamps.fideswiss.core;`. The compiler then enforces the library's exports at compile time, and the Spring module consumes it on the classpath. This fits the clean-architecture layout. I did not build a spike, so treat it as **[unverified]** until a small proof compiles under Maven with the Boot parent.
- I did not run a Boot fat jar against the library modules. Given the classpath behaviour above I expect no problem, but it is untested here.

## 4. Server-rendered htmx with Thymeleaf: established patterns

- **Fragments.** Define with `th:fragment`, include with `th:insert` or `th:replace`, reference with `~{template::selector}`. Fragments can take parameters and receive markup as arguments, with `~{}` (empty) and `_` (no-op) as special values (https://www.thymeleaf.org/doc/tutorials/3.1/usingthymeleaf.html). A controller can return a view name such as `"tournament/players :: playerTable"` to render one fragment.
- **Several fragments per response (out-of-band).** Spring MVC has `FragmentsRendering` (and `Collection<ModelAndView>`), which renders several views into one response, each with its own model, inheriting the request model (https://docs.spring.io/spring-framework/reference/web/webmvc-view/mvc-fragments.html). `htmx-spring-boot` shows the htmx pairing: an `@HxRequest` method returns `FragmentsRendering.with("users/list").fragment("users/count").build()`. The secondary fragments carry `hx-swap-oob="true"` and an `id` (https://htmx.org/docs/).
- **htmx-spring-boot** (https://github.com/wimdeblauwe/htmx-spring-boot): `@HxRequest` (map only htmx requests), `HtmxRequest`/`HtmxResponse`, response-header annotations (`@HxTrigger`, `@HxPushUrl`, `@HxReselect`), redirect views (`HtmxRedirectView`, `redirect:htmx:/path`), a Thymeleaf dialect (`hx:get="@{/users/{id}(id=${id})}"` renders `hx-get`), and automatic CSRF header injection for `hx:post/put/patch/delete`. Its Thymeleaf module's POM lists Spring Security as a dependency, so whether the CSRF support needs Spring Security at runtime is **[unverified]**. The app is single-user with no login at first, so CSRF matters little locally, but hosted mode will need it.
- **Layout.** Thymeleaf's own parameterised fragments (`th:replace="~{layout :: page(~{::title}, ~{::main})}"`) cover the shell/page split with fewer dependencies. The layout dialect (decorator style, `layout:decorate`) stays supported (4.0.1, BOM-managed). I did not check how Boot 4 wires it in. **[unverified]** Recommendation: fragment expressions first, dialect only if templates get awkward.
- **Form validation.** Bean Validation with `BindingResult`; re-render the form fragment with field errors (`th:errors`, `#fields.hasErrors`). Status code matters: htmx 2 does not swap 4xx/5xx by default, so a `422` needs `responseHandling` config or the response-targets extension (https://htmx.org/docs/). htmx 4 swaps every response except 204/304 (https://github.com/bigskysoftware/htmx/blob/v4.0.0/www/src/content/docs/whats-new-in-htmx-4.md). Simple portable choice: return 200 with the re-rendered form for htmx requests.
- **Progressive enhancement.** Write plain `<a href>` and `<form action method>` first; add `hx-boost="true"` on a container or `hx-*` on the element. Without JavaScript the server serves full pages; with htmx the server checks the `HX-Request` header (`@HxRequest`) and returns a fragment. Post/redirect/get stays valid: for htmx requests return `HX-Redirect` or `HX-Location` via the redirect views (https://htmx.org/docs/). Send `Vary: HX-Request` on any URL that serves both a page and a fragment, so caches do not mix them (general HTTP practice; not an htmx doc quote).
- **Security.** Thymeleaf escapes with `th:text`; avoid `th:utext` on user data. htmx docs advise `hx-disable` around raw user HTML, CSRF via `hx-headers`, and `allowEval:false` (https://htmx.org/docs/).

### htmx 2 versus 4 (why the recommendation is 2 now)

From the release notes and upgrade skill (https://github.com/bigskysoftware/htmx/blob/v4.0.0/CHANGELOG.md ; https://github.com/bigskysoftware/htmx/blob/v4.0.0/src/skills/htmx-upgrade-from-htmx2.md ; announcement https://four.htmx.org/announcements/2026-08-28-htmx-4.0.0-is-released):
- `fetch()` replaces `XMLHttpRequest`. Attribute inheritance becomes explicit (`hx-target:inherited`). 4xx/5xx responses swap. OOB and `hx-partial` content swaps after the main content. Events are renamed (`htmx:configRequest` becomes `htmx:config:request`). The request header `HX-Trigger` becomes `HX-Source` with a `tag#id` format. `hx-disable` and `hx-ignore` swap meaning. `hx-delete` no longer sends form data. An `htmx-2-compat` extension exists to restore old behaviour.
- htmx 2 is "supported indefinitely"; 2.x keeps npm/CDN `latest` until early 2027; upgrade tool `npx htmx.org@4.0.0 upgrade-check`.
- `htmx-spring-boot` 5.1.0 exposes trigger and target matching (`@HxRequest("id")`) built on `HX-Trigger`/`HX-Target`. Those semantics changed in 4, so that matching would need a library release aimed at htmx 4. I found no such release or issue. **[unverified]**

Write templates now so the move is cheap: put an explicit `hx-target` on every element, avoid inherited `hx-*` attributes, do not rely on OOB running before the main swap, and match `@HxRequest` on nothing more than the bare header.

## 5. Testing

- **Slice tests.** `@WebMvcTest` and `@AutoConfigureMockMvc` are in `org.springframework.boot.webmvc.test.autoconfigure` (artifact `spring-boot-webmvc-test`, pulled by `spring-boot-starter-webmvc-test`; verified in the 4.1.1 jar). Send `HX-Request: true` to test fragment paths. Assert on rendered HTML (`content().string(...)`, `xpath`, or jsoup). Boot 4 also adds `RestTestClient` on top of MockMvc (https://github.com/spring-projects/spring-boot/wiki/Spring-Boot-4.0-Release-Notes). `@MockitoBean` replaces the application-layer ports.
- **HtmlUnit.** Boot 4.1.1 keeps its MockMvc/HtmlUnit integration (`AutoConfigureMockMvc$HtmlUnit`, `MockMvcHtmlUnitDriverCustomizer`, `MockMvcWebDriverAutoConfiguration`; verified in the jar). Add `org.htmlunit:htmlunit` yourself; the BOM manages 4.21.0 and 5.5.0 exists. It fits "works without JavaScript" checks (links, forms, redirects, validation messages). It has **no native `fetch()`**: its changelog only lists an opt-in `setFetchPolyfillEnabled()`, so htmx 4 in HtmlUnit is doubtful. htmx 2 with `XMLHttpRequest` may partly work; I did not test it. **[unverified]** Do not rely on HtmlUnit to run htmx.
- **Playwright for Java 1.63.0** drives a real Chromium/Firefox/WebKit against a running app (`@SpringBootTest(webEnvironment = RANDOM_PORT)`). It is the right tool for the htmx behaviour (swaps, OOB, history). Cost: it downloads browsers on first run, so run it in a dedicated Maven profile or failsafe phase, not in the default unit run.
- Test pyramid suggestion: domain and application tests without Spring; MockMvc slices for controllers and fragments; a handful of Playwright journeys (create tournament, pair round, enter results, correct a result, export TRF).

## Open points for the next tickets

- Confirm with a spike that a Boot fat jar plus the library modules starts and pairs a round on the classpath (expected to work).
- Decide the compile-time encapsulation route (ArchUnit versus a `module-info` domain module).
- Recheck htmx-spring-boot for an htmx 4 release when Boot 4.2 goes GA.

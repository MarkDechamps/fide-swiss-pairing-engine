# Git hooks

Versioned git hooks for this repo.

## pre-commit

Runs the test suite before every commit and aborts the commit if tests fail.
It detects the build tool from the repo top level:

| Found                              | Command           |
|------------------------------------|-------------------|
| `mvnw`                             | `./mvnw -q test`  |
| `pom.xml`                          | `mvn -q test`     |
| `gradlew`                          | `./gradlew test`  |
| `build.gradle` / `build.gradle.kts`| `gradle test`     |
| none of the above                  | skip, allow commit|

## Enabling (required once per clone)

Git does not enable versioned hooks automatically. After cloning, run:

```sh
git config core.hooksPath .githooks
```

To bypass in an emergency: `git commit --no-verify`.

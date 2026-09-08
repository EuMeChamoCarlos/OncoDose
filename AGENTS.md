# AGENTS.md

Spring Boot 4.1.1 backend (oncoDoseAPI). Generated from Spring Initializr; still mostly a skeleton.

## Commands (Windows)
- Build/test: `.\mvnw.cmd test` (uses bundled Maven 3.9.16 wrapper — do not use a system `mvn`)
- Run dev server: `.\mvnw.cmd spring-boot:run`
- Single test: `.\mvnw.cmd test -Dtest=OncoDoseApiApplicationTests`
- Requires JDK 21 (project configured with temurin-21 in `.idea/misc.xml`)

## Framework gotchas
- **Spring Boot 4 uses modular starters.** Use `spring-boot-starter-webmvc`, `spring-boot-starter-security`, `spring-boot-starter-validation` (already declared in `pom.xml`). The old `spring-boot-starter-web` and test starters like `spring-boot-starter-webmvc-test` are split into `*-test` artifacts — do not re-introduce legacy starter names.
- Package root is `br.edu.ufersa.pw.oncodose.oncoDoseAPI` — note the mixed-case segment. Put all new classes under this tree.
- Lombok is already wired via explicit `annotationProcessorPaths` in `pom.xml` (main + test). Just use annotations; no extra config.
- Postgres JDBC driver is on the classpath but **no datasource config exists yet** in `application.properties`. `@SpringBootTest` (context test) currently loads without a DB. If you add JPA/spring-jdbc, supply datasource config and a running Postgres or the context test will fail.
- Spring Security starter is present with **no SecurityConfig** — default Basic auth with an auto-generated password protects all endpoints until you add one.
- DevTools is a runtime dep (auto-restart on changes); harmless during dev.

## Repo state
- No code beyond the `@SpringBootApplication` bootstrap + context-load test.
- Not yet a git repo (`.gitignore`/`.gitattributes` ready); run `git init` before first commit.
- `HELP.md` is Initializr boilerplate and gitignored — ignore it.
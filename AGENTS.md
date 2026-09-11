# AGENTS.md

Spring Boot 4.1.1 backend (oncoDoseAPI). Requires JDK 21 (`JAVA_HOME=C:\Users\Segundo\.jdks\temurin-21.0.12`).

## Commands (Windows)
- Build/test: `.\mvnw.cmd test` (uses bundled Maven 3.9.16 wrapper — do not use a system `mvn`)
- Run dev server: `.\mvnw.cmd spring-boot:run`
- Single test: `.\mvnw.cmd test -Dtest=OncoDoseApiApplicationTests`
- Compile only: `.\mvnw.cmd -q compile -DskipTests`

## Framework gotchas
- **Spring Boot 4 uses modular starters.** Use `spring-boot-starter-webmvc`, `spring-boot-starter-security`, `spring-boot-starter-validation` (already declared in `pom.xml`). The old `spring-boot-starter-web` and test starters like `spring-boot-starter-webmvc-test` are split into `*-test` artifacts — do not re-introduce legacy starter names.
- Package root is `br.edu.ufersa.pw.oncodose.oncoDoseAPI` — note the mixed-case segment. Put all new classes under this tree (NOT `br.edu.ufersa.oncodose` from the refactor docs).
- Lombok is already wired via explicit `annotationProcessorPaths` in `pom.xml` (main + test). Just use annotations; no extra config.
- Postgres + Flyway V1-V3 applied (`jdbc:postgresql://localhost:5432/onco_dose`, `ddl-auto=validate`). Context test needs a running Postgres.
- Security: `SecurityConfig` + `SecurityFilter` + JWT (`ROLE_USER`/`ROLE_ADMIN`, hierarchy `ADMIN → USER`). `@PreAuthorize` failures are mapped to 403 `ProblemDetail` by `GlobalExceptionHandler`, not by the filter chain.
- `HELP.md` is Initializr boilerplate and gitignored — ignore it.

## Architecture rules (Prof. Gadelha, v1+v2 merged)
- `DomainException extends RuntimeException` in `domain.exception`, fail-fast with rich context. Never `ResponseStatusException` in business code, never `catch (Exception)` swallowing.
- `GlobalExceptionHandler` (`application.api.exception`) returns RFC 7807 `ProblemDetail` + `timestamp`; never 200-with-error, never leak stacktrace.
- UseCases in `application.usecases` own `@Transactional`; `domain.services` is pure logic with zero infra; controllers are thin (`@Valid` DTO → UseCase → `ResponseEntity` 200/201+`Location`/204). No Repository injection in controllers.
- No MapStruct yet, no Records DTOs yet, no `/api/v1` yet — Fase 3-7 pending.

## AI working agreement (adapted from CLAUDE.md)
- Triage every task before work, printed first: `Size: small|medium|large — why / Tests: touched-module|full suite — why / Branch: <task-named branch> from <base>`.
  - small: 1-2 files, no behavior change → touched tests only, solo, no commit ceremony beyond user validation.
  - medium: localized behavior change in one module → touched suite + regression test, one cold re-read of the diff before reporting.
  - large: cross-module/contract/architecture → full protocol, full suite, explicit validation plan.
  - When torn, pick the smaller size and say so; escalate with a fresh triage block the moment scope grows.
- One task, one branch, task-named kebab-case (`feature/<what-changes>`). Never phase names, never work on `master` directly. Base is the last validated branch, not `origin/main` blindly.
- NOT adopted from CLAUDE.md (conflicts with this repo): git worktrees/session isolation (single Windows checkout, no such tool here), auto commit/push/PR after every task. Here: AI implements + compiles + tests, user validates via Postman, user commits. AI only commits/pushes on explicit request. Never push to `main`/`master`.
- Verify, don't guess: `compile` before reporting; `test` for behavior changes; never inherit a claim from an earlier draft. Manual Postman steps ship with every behavior change (method, URL, body, expected status/title/detail).
- Search before building; tie each change to an observable outcome (status code, header, payload field).
- Safety: never commit secrets (check `.gitignore` if `.env` touched); never `rm -rf`, `git reset --hard`, `DROP TABLE`, or similar without explicit confirmation; never `--no-verify`.
- Confusion protocol: on architectural ambiguity or destructive scope, stop, name it in one sentence, offer 2-3 options with trade-offs, ask.
- Talk direct and short, concrete `file:line`, no AI vocabulary dumps. End with the next action, not a recap.
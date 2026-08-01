# Simpsons Rest Assured Tests

![Java](https://img.shields.io/badge/Java-21-orange)
![Maven](https://img.shields.io/badge/Maven-3.9-blue)
![Build](https://img.shields.io/github/actions/workflow/status/Michael0967/simpsons-rest-assured-tests/ci.yml)
![License](https://img.shields.io/badge/license-MIT-green)

API testing framework for [The Simpsons API](https://thesimpsonsapi.com/), built with Rest Assured + JUnit 5 + Maven. Portfolio project.

## Stack

| Component         | Library                          |
|-------------------|----------------------------------|
| HTTP / validation | Rest Assured 5.5                 |
| Runner            | JUnit 5 (Jupiter)                |
| Contracts         | JSON Schema (json-schema-validator) |
| Assertions        | Hamcrest + AssertJ               |
| Deserialization   | Jackson                          |
| Reporting         | Allure                           |
| Logging           | Logback                          |

## Requirements

- JDK 21 (`export JAVA_HOME=/opt/homebrew/opt/openjdk@21`)
- Maven 3.9+

## Installation

```bash
git clone git@github.com:Michael0967/simpsons-rest-assured-tests.git
cd simpsons-rest-assured-tests
mvn test
```

## Execution

```bash
mvn test                            # full suite
mvn test -Psmoke                    # quick happy-path only
mvn test -Pcontract                 # contract validation only
mvn test -Pregression               # functional coverage
mvn test -Pperformance              # latency SLA + concurrent load
mvn test -Psecurity                 # security (headers, injection, methods)
mvn test -Pfuzz                     # robustness against random/extreme input
mvn test -Dgroups="smoke,contract"  # combined tags
mvn test -Dsurefire.parallel=false  # disable parallel execution
```

## Static analysis

Quality gates run separately so they don't slow down the suite:

```bash
mvn verify -Pstatic-analysis   # PMD + SpotBugs on the test code
mvn verify -Psecurity-scan     # OWASP dependency-check (CVE in dependencies)
```

- **PMD** (`src/test/resources/pmd/ruleset.xml`): best practices, error-prone and
  performance rules. Fails the build on violations.
- **SpotBugs** (`src/test/resources/spotbugs/exclude.xml`): bytecode analysis at
  Max effort; only mutable-list exposures in test-data POJOs are excluded.
- **OWASP dependency-check**: downloads the NVD database on first run and fails
  the build if any dependency has a CVE with CVSS >= 7.

## Allure report

```bash
mvn test
brew install allure          # once
allure serve target/allure-results
```

## Configuration

Defaults live in `src/test/resources/config.properties` and can be overridden without editing it:

```bash
mvn test -Dapi.base.uri=https://thesimpsonsapi.com
API_BASE_URI=https://thesimpsonsapi.com mvn test   # environment variable
```

## Architecture

```
src/test/java/com/simpsons/
├── core/            # ApiConfig (external config), RestClient (timeouts + filters)
├── client/          # SimpsonsApiClient: typed endpoint facade
├── filters/         # RetryFilter (429/5xx), ApiLogFilter (cross-cutting logging)
├── data/            # DataReader + test data records (external data)
├── models/          # POJOs (Character, Episode, Location, PaginatedResponse, ErrorResponse)
├── validation/      # ApiSchemaValidator (JSON Schema)
└── tests/           # smoke, characters, episodes, locations, pagination,
                     # errorhandling, contract, security, performance, fuzz
src/test/resources/
├── config.properties
├── data/            # external test data in JSON
├── schemas/         # JSON Schema contracts
└── logback-test.xml
```

## CI

[GitHub Actions](.github/workflows/ci.yml) runs the full suite on every push to
`main` and on pull requests, using Java 21 (Temurin) + Maven.

## Design decisions (senior QA)

- **Service layer**: tests call `SimpsonsApiClient` (typed methods), never Rest Assured directly. An endpoint change is resolved in one place.
- **External config**: URL, timeouts and retries come from `config.properties`, overridable via `-D` or environment variables.
- **Anti-flakiness**: `RetryFilter` retries with exponential backoff on 429/5xx and connection failures; explicit connect/read timeouts.
- **External data**: characters/episodes/locations to validate live in `data/*.json`, not hardcoded in tests. Tests are `@ParameterizedTest`.
- **Contracts**: JSON Schemas in `schemas/` validate structure; `error.json` also covers error responses.
- **Consistency tests**: a character's first appearance is cross-checked against the episodes endpoint; pagination invariants (count = 20·(pages-1) + last page) are verified per resource.
- **Tagged by level**: `smoke` / `regression` / `contract` / `performance` / `security` / `fuzz` for layered CI execution.
- **Black-box security**: mandatory TLS, security headers, SQLi/XSS/path-traversal rejection, numeric overflow, no internal-detail or sensitive-field leaks, dangerous HTTP methods without effects.
- **Performance with SLA config**: average and p95 latency per endpoint against thresholds in `config.properties`; concurrent load test requiring 100% 2xx responses.
- **Robustness/fuzz**: "never 5xx" invariant against random/extreme IDs and strings, extreme pages, unsupported content negotiation, malformed headers, odd paths and unicode.
- **Static analysis**: PMD + SpotBugs (`-Pstatic-analysis`) and OWASP dependency-check (`-Psecurity-scan`).

## Useful commands

```bash
export JAVA_HOME=/opt/homebrew/opt/openjdk@21   # add to ~/.zshrc
mvn test
```

## License

[MIT](LICENSE)

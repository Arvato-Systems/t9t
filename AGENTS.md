# AGENTS.md

## Repository overview
- This repository contains the t9t enterprise Java framework and is organized as a large multi-module Maven build.
- The root build requires Java 21 (`pom.xml`) and currently documents Maven 3.9.9 as the minimum Maven version.

## Working rules for agents
- Keep changes as small and focused as possible.
- Prefer targeted module-level validation over broad repository-wide commands.
- Update nearby documentation when behavior or developer workflows change.

## Build and test caveats
- The dependency management in `global-dm/pom.xml` imports `de.jpaw` BOMs, and the root build also relies on Bonaparte DSL tooling declared in `pom.xml`.
- Maven builds can fail when prerequisite artifacts from the related `jpaw`, `bonaparte-java`, and `bonaparte-dsl` repositories are not available via GitHub Packages or prior local builds. The packages should exist under https://github.com/arvato-systems-jacs/ .

## Useful locations
- `README.md` for top-level project information.
- `docs/` for repository documentation.
- `checkstyle.xml` for style rules.

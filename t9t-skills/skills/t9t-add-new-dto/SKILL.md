---
name: t9t-add-new-dto
description: "Guide for adding a new DTO or entity in the t9t repository or a t9t-based application. Use when asked to add a DTO, entity, CRUD/search requests, grid config, SQL migration, or related ZK screen wiring."
license: Apache-2.0
---

# t9t: add a new DTO

Use this skill when the task is to add a new DTO / entity in `t9t` or a `t9t`-based application.

## Required input

Before writing code, verify the task provides all required inputs listed in [`references/required-inputs.md`](references/required-inputs.md).

If critical inputs are missing, ask for them first. If the request is too incomplete to implement safely, reject the task instead of guessing.

## Workflow

Copy and track this checklist:

```text
- [ ] Verify the required inputs and identify the target modules
- [ ] Add or confirm the BON DTO definitions in the `-api` module
- [ ] Add the BON request definitions for CRUD/search (and lean search when eligible)
- [ ] Register the view model and add translations
- [ ] Add the grid configuration JSON
- [ ] Add the entity DSL, resolver, mapper, and JPA request handlers
- [ ] Build once so generated SQL exists, then create the migration file
- [ ] Add the test extension helper when tests in the target area use that pattern
- [ ] Add the ZUL screen, icon, and menu registration when UI work is required
- [ ] Run targeted validation and confirm the changed paths are complete
```

## Implementation rules

1. Start by reading [`references/implementation-checklist.md`](references/implementation-checklist.md).
2. Also inspect the existing module for analogous DTOs, request handlers, screens, and migrations before editing.
3. Preserve existing package names, module boundaries, file naming conventions, and menu placement patterns.
4. Do not invent RTTI values, natural keys, labels, menu sections, or screen positions. Ask when missing.
5. Only create the optional lean-search handler when the natural key has a single field.
6. Do not add `@AllCanAccessGlobalTenant` or `@GlobalTenantCanAccessAll` unless the requester explicitly specifies the intended global-tenant authorization; analogous code may be consulted for syntax, but must not be used to infer authorization.
7. For JPA request handlers, use an existing handler in the same repository as the concrete template, especially the `CsvConfiguration` handlers mentioned in the reference.
8. After the code compiles, create the SQL migration from the generated SQL instead of hand-writing table details from scratch.

## Output contract

Before finishing, confirm that:

1. Every required repository layer for the requested DTO exists or was intentionally skipped because it was not part of the request.
2. Optional steps were only applied when their prerequisites were met.
3. Missing business inputs were surfaced to the requester instead of guessed.
4. The implementation was validated with targeted checks for the changed area.

---
name: fix
description: Use after completing any code changes (new features, bug fixes, refactors) before reporting completion
---

Run the auto-fix and validation suite for the GLSP Eclipse Integration. The repository has two
halves that are validated separately — run the part your change touched, and both when the change
spans client and server.

## Client

All commands run from the `client/` directory, the pnpm workspace root.

1. Build first (compiles the TypeScript packages and bundles the workflow webapp). This is a hard gate: if the build fails, stop immediately, report the build errors, and do not run any of the following steps.
   The build must pass before anything else runs.

```bash
pnpm build
```

2. Auto-fix lint, formatting, and copyright headers. Run all three even if an earlier one reports remaining problems (they are independent):

```bash
pnpm lint:fix
pnpm format
pnpm headers:fix
```

Then:

- If `pnpm build` failed, fix the compile errors and re-run this skill.
- If `pnpm lint:fix` reported lint errors it could not fix, fix them manually and re-run this skill.

## Server

The workflow example plugin ships the bundled webapp, so a client change reaches the server build
only after it has been copied over. Run this from the repository root:

```bash
pnpm -C client copy:client
```

Then compile, run checkstyle and the tests in one pass and turn the collected violations into a
verdict. Run the summary even when the Maven build failed — the reports of the modules that were
built are still worth reading:

```bash
mvn -f server clean verify -B -Dcheckstyle.failOnViolation=false
.github/scripts/checkstyle-summary.py
```

Then:

- If the Maven build failed, fix the compile errors or failing tests and re-run this skill.
- If the checkstyle summary reported violations, fix them and re-run this skill.

Report completion once everything is clean: the client builds with no remaining lint errors and
with formatting and headers corrected in place, and the server build, tests and checkstyle pass.

Notes:

- Checkstyle runs in the `validate` phase, so letting it fail the build would skip the rest of the build. `-Dcheckstyle.failOnViolation=false` collects the violations instead and the summary script turns them into the failure — the same split the CI workflow uses.
- `pnpm headers:fix` is run from `client/` but checks the whole repository, Java sources included.

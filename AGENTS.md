# Repository Guidelines

## Project Structure & Module Organization
This repository is split into two main parts:
- `client/`: TypeScript/Yarn workspace for the GLSP IDE integration package and workflow webapp example.
  - `client/packages/ide/src/`: reusable integration source.
  - `client/examples/workflow-webapp/src/`: example app source, bundled to `client/examples/workflow-webapp/app/`.
- `server/`: Java 17 + Maven/Tycho Eclipse plugin build.
  - `server/plugins/org.eclipse.glsp.ide.editor/src/`: core Eclipse integration plugin.
  - `server/example/org.eclipse.glsp.ide.workflow.editor/src/`: workflow editor example plugin.
  - `server/releng/`: target platform, feature, and p2 repository modules.

## Build, Test, and Development Commands
- `cd client && yarn install`: install workspace dependencies (Node `>=20`, Yarn 1.x).
- `cd client && yarn build`: compile TS packages and bundle the workflow webapp.
- `cd client && yarn lint`: run ESLint on all TS/TSX sources.
- `cd client && yarn format:check`: verify Prettier formatting.
- `cd client && yarn check:pr`: CI-like client gate (`build`, `lint`, `format:check`, headers).
- `cd client/examples/workflow-webapp && yarn test`: run Mocha specs (`*.spec.ts|tsx`) for the example app.
- `cd server && mvn clean install`: build Eclipse bundles.
- `cd server && mvn clean install -Pp2`: build including p2 artifacts.
- `cd server && mvn checkstyle:check -B`: run Java style checks.

## Coding Style & Naming Conventions
- TypeScript: 4-space indentation, single quotes, trailing commas disabled, max line length 140 (see `.prettierrc`).
- Linting: ESLint with `@eclipse-glsp` config (`client/.eslintrc.js`).
- Java: follow Checkstyle rules configured via Maven (`maven-checkstyle-plugin`).
- Naming: keep existing conventions (`*.spec.ts` for tests, PascalCase Java classes, kebab-case TS file names where already used).

## Testing Guidelines
Prefer targeted tests for changed behavior:
- Client example tests live beside sources and should match `*.spec.ts`/`*.spec.tsx`.
- Run `yarn check:pr` for client-side validation before opening a PR.
- For server changes, run `mvn clean verify` (or at least `mvn clean install`) and `mvn checkstyle:check`.

## Commit & Pull Request Guidelines
- Create/track an umbrella issue first (`https://github.com/eclipse-glsp/glsp/issues/...`).
- Branch naming in upstream repos follows `issues/{issue_number}`.
- Reference the full issue URL in commit messages (not just `#123`).
- Keep commit subjects short, imperative, and specific (history examples include `GLSP-1607: Fix deployment`).
- PRs should include scope, rationale, test evidence (commands run), and screenshots/GIFs for UI changes.

# AGENTS.md

- The repository has two halves: `client/` is a pnpm workspace (never npm or yarn), `server/` is the Maven/Tycho build of the Eclipse plugins.
- The workflow example plugin ships the bundled webapp. After client changes run `pnpm copy:client` in `client/` before building the server, otherwise the plugin keeps the previous bundle.
- Consult `README.md`, `client/README.md` and `server/README.md` for the module layout and the development setup.
- Document public APIs with TSDoc and Javadoc and use `{@link Symbol}` for cross-references. Explain behavior and non-obvious decisions rather than restating signatures.
- After code changes, run the /fix skill. Resolve failures and repeat until build, lint, formatting, headers, and checkstyle pass.
- `CHANGELOG.md` is generated from the merged PRs before a release. Do not add or bump entries manually.

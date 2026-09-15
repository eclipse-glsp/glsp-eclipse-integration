# Eclipse GLSP Eclipse IDE Integration webapp [![CI (Client)](https://github.com/eclipse-glsp/glsp-eclipse-integration/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/eclipse-glsp/glsp-eclipse-integration/actions/workflows/ci.yml)

Contains the client side glue code for opening browser-based GLSP diagrams in an Eclipse IDE editor as well as the workflow webapp example.
This project is available from npm via [@eclipse-glsp/ide](https://www.npmjs.com/package/@eclipse-glsp/ide).

## Developer Documentation

### First time setup

- Install [node.js](https://nodejs.org/) (requires Node v22+)
- Install pnpm: <https://pnpm.io/installation> (use pnpm 11+); a recent pnpm automatically switches to the version pinned in the `packageManager` field
- Clone this repository
- Install dependencies from this `client` directory: `pnpm i` or `pnpm i --frozen-lockfile`

### Build & Testing

- Build (all packages + workflow webapp bundle): `pnpm build`
- Lint: `pnpm lint`
- Check formatting: `pnpm format:check`
- Clean (all packages): `pnpm clean`
- `pnpm copy:client` copies the bundled webapp into the Eclipse server's diagram folder (`../server/example/org.eclipse.glsp.ide.workflow.editor/diagram`); the CI build runs this automatically before building the server.

## More information

For more information, please visit the [Eclipse GLSP Umbrella repository](https://github.com/eclipse-glsp/glsp) and the [Eclipse GLSP Website](https://www.eclipse.org/glsp/).
If you have questions, please raise them in the [discussions](https://github.com/eclipse-glsp/glsp/discussions) and have a look at our [communication and support options](https://www.eclipse.org/glsp/contact/).

https://user-images.githubusercontent.com/588090/161574983-ce70ecae-d322-472a-a80a-e9c9e3e17b1d.mp4

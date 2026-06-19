# Eclipse GLSP Eclipse IDE Integration webapp [![build-status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fglsp%2Fjob%2Feclipse-glsp%2Fjob%2Fglsp-eclipse-integration%2Fjob%2Fmaster%2F)](https://ci.eclipse.org/glsp/job/eclipse-glsp/job/glsp-eclipse-integration/job/master/)

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
- `pnpm copy:client` copies the bundled webapp into the Eclipse server's diagram folder (`../server/example/org.eclipse.glsp.ide.workflow.editor/diagram`); the Jenkins build runs this automatically before building the server.

## More information

For more information, please visit the [Eclipse GLSP Umbrella repository](https://github.com/eclipse-glsp/glsp) and the [Eclipse GLSP Website](https://www.eclipse.org/glsp/).
If you have questions, please raise them in the [discussions](https://github.com/eclipse-glsp/glsp/discussions) and have a look at our [communication and support options](https://www.eclipse.org/glsp/contact/).

https://user-images.githubusercontent.com/588090/161574983-ce70ecae-d322-472a-a80a-e9c9e3e17b1d.mp4

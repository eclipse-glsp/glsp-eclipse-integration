# Eclipse GLSP Eclipse IDE Integration [![CI (Server)](https://github.com/eclipse-glsp/glsp-eclipse-integration/actions/workflows/ci-server.yml/badge.svg?branch=master)](https://github.com/eclipse-glsp/glsp-eclipse-integration/actions/workflows/ci-server.yml) [![Deploy](https://github.com/eclipse-glsp/glsp-eclipse-integration/actions/workflows/deploy.yml/badge.svg?branch=master)](https://github.com/eclipse-glsp/glsp-eclipse-integration/actions/workflows/deploy.yml)

Contains the glue code for opening browser-based GLSP diagrams in an Eclipse IDE Editor

## Building

The GLSP Eclipse integration bundles are a Tycho build and are built with `mvn clean verify` (Java 21 or higher).
The workflow example bundle embeds the webapp from [`client`](../client/README.md), so build and copy it in first (`pnpm build && pnpm copy:client` from the `client` directory) if you need a complete bundle.

### P2 Update Sites

- _Snapshots:_ <https://download.eclipse.org/glsp/ide/p2/nightly/>
- _Release Candidates:_ <https://download.eclipse.org/glsp/ide/p2/staging/>
- _Releases:_ <https://download.eclipse.org/glsp/ide/p2/releases/>

The nightly composite update site is maintained by [`p2-composite.sh`](releng/org.eclipse.glsp.ide.repository/p2-composite.sh), which the `Deploy` workflow invokes after a successful CI build on `master`.

## More information

For more information, please visit the [Eclipse GLSP Umbrella repository](https://github.com/eclipse-glsp/glsp) and the [Eclipse GLSP Website](https://www.eclipse.org/glsp/).
If you have questions, please raise them in the [discussions](https://github.com/eclipse-glsp/glsp/discussions) and have a look at our [communication and support options](https://www.eclipse.org/glsp/contact/).

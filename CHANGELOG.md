# Eclipse GLSP Eclipse Integration Changelog

## v1.1.0 - upcoming

### Changes

- [diagram] Fixed a bug that could cause a crash when closing a diagram editor on Windows [#59](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/59)
- [debug] Fixed a bug to ensure that the system browser opens reliably when using the `Debug (External Browser)` command [#60](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/60)
- [websocket] Fixed a bug that could trigger premature session disposal even if other GLSP clients where associated with this session [#63](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/63)
- [eclipse] Propagate the GLSP diagram selection to the Eclipse selection service [#73](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/73) -- Contributed on behalf of STMicroelectronics

### Breaking Changes

- [websocket] Update to Jetty Websocket 10 [#70](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/70) - Contributed on behalf of STMicroelectronics
  - This includes breaking changes due to major API changes in Jetty and the following new minimum versions:
    - Jetty 9.x -> Jetty 10.0.13
    - LSP4J -> 0.8.0 -> 0.11.0
    - ELK 0.7.0 -> 0.8.1
    - Log4J 1.2x -> 2.19
  - Remove the `keepAliveModule` and rely on Jetty's infinite session timeout mechanism instead

## [v1.0.0 - 30/06/2022](https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v1.0.0)

### Changes

- [diagram] Fixed a bug that prevented successful completion the `DefaultModelInitializationConstraint` [#47](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/47)
- [editor] Made editor retrieval thread safe to ensure that actions that are sent after the editor has been disposed are correctly handled (i.e. ignored) [#48](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/48)
- [example] Improved and modernized styling of the GLSP workflow example [#49](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/49)
- [editor] Fixed a bug to ensure that keybindings are also working when using `WebView2` [#54](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/54/)

### Breaking Changes

- [build] Removed dependency to `Apache Commons IO` [#52](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/52)
- [gmodel] Rename handlers and services that operate directly on the GModels [#53](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/53):
  - `IdePasteOperationHandler` -> `GModelIdePasteOperationHandler`

## [v0.9.0- 09/12/2021](https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v0.9.0)

Inception of the Eclipse GLSP IDE integration.
This project provides the glue code for integrating browser-based GLSP diagram into the Eclipse IDE.

# Eclipse GLSP Eclipse Integration Changelog

## v2.1.0 - 24/01/2024(<https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v2.1.0>)

## v2.0.0 - 14/10/2023(<https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v2.0.0>)

### Changes

-   [diagram] Fix a bug that could cause a crash when closing a diagram editor on Windows [#59](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/59)
-   [debug] Fix a bug to ensure that the system browser opens reliably when using the `Debug (External Browser)` command [#60](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/60)
-   [websocket] Fix a bug that could trigger premature session disposal even if other GLSP clients where associated with this session [#63](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/63)
-   [eclipse] Propagate the GLSP diagram selection to the Eclipse selection service [#73](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/73) -- Contributed on behalf of STMicroelectronics
-   [eclipse] Fix a dead-lock bug that could occur saving by closing a dirty editor [#75](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/75)
-   [diagram] Add context menu support for Windows (Edge) [#77](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/77)
-   [websocket] Implement websocket reconnect handling in example app [#84](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/84)
-   [server] Ensure that resources with a symlink a correctly resolved by the Jetty server on Windows [#85](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/85)
-   [deps] Remove upper bound version requirements for Guava and Guice to stay compatible with newer Eclipse versions [#89](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/89)

### Breaking Changes

-   [websocket] Update to Jetty Websocket 10 [#70](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/70) - Contributed on behalf of STMicroelectronics
    -   This includes breaking changes due to major API changes in Jetty and the following new minimum versions:
        -   Jetty 9.x -> Jetty 10.0.13
        -   LSP4J -> 0.8.0 -> 0.11.0
        -   ELK 0.7.0 -> 0.8.1
        -   Log4J 1.2x -> 2.19
    -   Remove the `keepAliveModule` and rely on Jetty's infinite session timeout mechanism instead
-   [eclipse] Rework of the GLSPDiagramEditor to support E4 Parts [#83](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/83)
    -   Most methods in GLSPDiagramEditor moved to GLSPDiagramComposite if you want to override them with your own implementation override GLSPDiagramEditor.createGLSPDiagramComposite() and return our own class
    -   Extent GLSPDiagramPart to implement your own E4 Part
    -   GLSPEditorRegistry works on GLSPDiagramComposite and no longer on GLSPDiagramEditor, which is the implementation used by E3 and E4
-   [client] Remove dependency to `vscode-ws-jsonrpc`. Use websocket handling provided by glsp-client instead [#76](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/76)
-   [deps] Use Java 17 as minimum compilation target since Java 11 is EOL [#91](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/91)

## [v1.0.0 - 30/06/2022](https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v1.0.0)

### Changes

-   [diagram] Fix a bug that prevented successful completion the `DefaultModelInitializationConstraint` [#47](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/47)
-   [editor] Make editor retrieval thread safe to ensure that actions that are sent after the editor has been disposed are correctly handled (i.e. ignored) [#48](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/48)
-   [example] Improve and modernize styling of the GLSP workflow example [#49](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/49)
-   [editor] Fix a bug to ensure that keybindings are also working when using `WebView2` [#54](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/54/)

### Breaking Changes

-   [build] Remove dependency to `Apache Commons IO` [#52](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/52)
-   [gmodel] Rename handlers and services that operate directly on the GModels [#53](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/53):
    -   `IdePasteOperationHandler` -> `GModelIdePasteOperationHandler`

## [v0.9.0- 09/12/2021](https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v0.9.0)

Inception of the Eclipse GLSP IDE integration.
This project provides the glue code for integrating browser-based GLSP diagram into the Eclipse IDE.

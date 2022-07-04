# Eclipse GLSP Eclipse Integration Changelog

## [v1.0.0 - 30/06/2022](https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v1.0.0)

### Changes

-   [diagram] Fixed a bug that prevented successful completion the `DefaultModelInitializationConstraint` [#47](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/47)
-   [editor] Made editor retrieval thread safe to ensure that actions that are sent after the editor has been disposed are correctly handled (i.e. ignored) [#48](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/48)
-   [example] Improved and modernized styling of the GLSP workflow example [#49](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/49)
-   [editor] Fixed a bug to ensure that keybindings are also working when using `WebView2` [#54](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/54/)

### Breaking Changes

-   [build] Removed dependency to `Apache Commons IO` [#52](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/52)
-   [gmodel] Rename handlers and services that operate directly on the GModels [#53](https://github.com/eclipse-glsp/glsp-eclipse-integration/pull/53):
    -   `IdePasteOperationHandler` -> `GModelIdePasteOperationHandler`

## [v0.9.0- 09/12/2021](https://github.com/eclipse-glsp/glsp-eclipse-integration/releases/tag/v0.9.0)

Inception of the Eclipse GLSP IDE integration.
This project provides the glue code for integrating browser-based GLSP diagram into the Eclipse IDE.

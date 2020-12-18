# Eclipse GLSP Eclipse IDE Integration

[![build-status](https://img.shields.io/jenkins/build?jobUrl=https%3A%2F%2Fci.eclipse.org%2Fglsp%2Fjob%2Feclipse-glsp%2Fjob%2Fglsp-eclipse-integration%2Fjob%2Fmaster%2F)](https://ci.eclipse.org/glsp/job/eclipse-glsp/job/glsp-eclipse-integration/)
[![publish-status-npm](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/glsp/job/deploy-npm-glsp-theia-integration/&label=npm)](https://ci.eclipse.org/glsp/job/deploy-npm-ide-integration/)
[![publish-status-p2](https://img.shields.io/jenkins/build?jobUrl=https://ci.eclipse.org/glsp/job/deploy-npm-glsp-theia-integration/&label=p2)](https://ci.eclipse.org/glsp/job/deploy-p2-ide-integration/)

Contains the glue code for opening browser-based GLSP diagrams in an Eclipse IDE Editor

# Workflow Diagram Example
The workflow diagram is a consistent example provided by all GLSP components. The example implements a simple flow chart diagram editor with different types of nodes and edges (see screenshot below). The example can be used to try out different GLSP features, as well as several available integrations with IDE platforms (Theia, VSCode, Eclipse, Standalone).
As the example is fully open source, you can also use it as a blueprint for a custom implementation of a GLSP diagram editor.
See [our project website](https://www.eclipse.org/glsp/documentation/#workflowoverview) for an overview of the workflow example and all components implementing it.

![Workflow Diagram](/documentation/glsp-eclipse-integration-animated.gif)

## How to start the Workflow Diagram example?
First, you need to build both the client and the server (See the Building section below). Then, start an Eclipse Application containing the org.eclipse.glsp.ide.workflow.editor plug-in (e.g. using the `WorkflowEditor.launch` launch configuration provided in that project). The Workflow Diagram Editor is registered for *.wf files. You can use the provided example project: `server/example/runtime/test`

## Where to find the sources?
In addition to this repository, the related source code can be found here:
- https://github.com/eclipse-glsp/glsp-server
- https://github.com/eclipse-glsp/glsp-client

# Building

For details on building the project, please see both README files in the directories [`client`](client/README.md) and [`server`](server/README.md).

# More information
For more information, please visit the [Eclipse GLSP Umbrella repository](https://github.com/eclipse-glsp/glsp) and the [Eclipse GLSP Website](https://www.eclipse.org/glsp/). If you have questions, contact us on our [spectrum chat](https://spectrum.chat/glsp/) and have a look at our [communication and support options](https://www.eclipse.org/glsp/contact/).

/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
import "reflect-metadata";

import {
    EnableToolPaletteAction,
    GLSPActionDispatcher,
    GLSPDiagramServer,
    InitializeClientSessionAction,
    RequestTypeHintsAction
} from "@eclipse-glsp/client";
import { ApplicationIdProvider, BaseJsonrpcGLSPClient, JsonrpcGLSPClient } from "@eclipse-glsp/protocol";
import { CenterAction, RequestModelAction, TYPES } from "sprotty";

import createContainer from "./di.config";
import { getParameters } from "./url-parameters";

const urlParameters = getParameters();
const filePath = urlParameters.path;

// In the Eclipse Integration, port is dynamic, as multiple editors
// and/or Eclipse Servers may be running in parallel (e.g. 1/Eclipse IDE)
const port = parseInt(urlParameters.port);
const id = "workflow";
const name = "Workflow Diagram";
const websocket = new WebSocket(`ws://localhost:${port}/${id}`);

const container = createContainer();

const diagramServer = container.get<GLSPDiagramServer>(TYPES.ModelSource);
if (urlParameters.client) {
    diagramServer.clientId = urlParameters.client;
}

const actionDispatcher = container.get<GLSPActionDispatcher>(TYPES.IActionDispatcher);

websocket.onopen = () => {
    const connectionProvider = JsonrpcGLSPClient.createWebsocketConnectionProvider(websocket);
    const glspClient = new BaseJsonrpcGLSPClient({ id, name, connectionProvider });
    diagramServer.connect(glspClient).then(client => {
        client.initializeServer({ applicationId: ApplicationIdProvider.get() });
        actionDispatcher.dispatch(new InitializeClientSessionAction(diagramServer.clientId));
        actionDispatcher.dispatch(new RequestModelAction({
            sourceUri: 'file://' + filePath,
            diagramType: "workflow-diagram",
        }));
        actionDispatcher.dispatch(new RequestTypeHintsAction("workflow-diagram"));
        actionDispatcher.dispatch(new EnableToolPaletteAction());
        actionDispatcher.onceModelInitialized().then(() => actionDispatcher.dispatch(new CenterAction([])));
    });
};


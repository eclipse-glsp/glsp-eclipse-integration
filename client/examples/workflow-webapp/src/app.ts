/********************************************************************************
 * Copyright (c) 2020-2023 EclipseSource and others.
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
import {
    BaseJsonrpcGLSPClient,
    EnableToolPaletteAction,
    GLSPActionDispatcher,
    GLSPDiagramServer,
    GLSPWebSocketProvider,
    RequestModelAction,
    RequestTypeHintsAction,
    ServerMessageAction,
    ServerStatusAction,
    TYPES,
    configureServerActions
} from '@eclipse-glsp/client';
import { getParameters } from '@eclipse-glsp/ide';
import { ApplicationIdProvider, GLSPClient } from '@eclipse-glsp/protocol';
import { MessageConnection } from 'vscode-jsonrpc';
import createContainer from './di.config';

const urlParameters = getParameters();
const filePath = urlParameters.path;

// In the Eclipse Integration, port is dynamic, as multiple editors
// and/or Eclipse Servers may be running in parallel (e.g. 1/Eclipse IDE)
const port = parseInt(urlParameters.port, 10);
const applicationId = urlParameters.application;
const id = 'workflow';
const diagramType = 'workflow-diagram';

const clientId = urlParameters.client || ApplicationIdProvider.get();
const widgetId = urlParameters.widget || clientId;
setWidgetId(widgetId);

let container = createContainer(widgetId);
let diagramServer = container.get<GLSPDiagramServer>(TYPES.ModelSource);
diagramServer.clientId = clientId;

const webSocketUrl = `ws://localhost:${port}/${id}`;

const wsProvider = new GLSPWebSocketProvider(webSocketUrl);
wsProvider.listen({ onConnection: initialize, onReconnect: reconnect, logger: console });

async function initialize(connectionProvider: MessageConnection, isReconnecting = false): Promise<void> {
    const client = new BaseJsonrpcGLSPClient({ id, connectionProvider });

    await diagramServer.connect(client);
    const result = await client.initializeServer({
        applicationId,
        protocolVersion: GLSPClient.protocolVersion
    });
    await configureServerActions(result, diagramType, container);

    await client.initializeClientSession({ clientSessionId: diagramServer.clientId, diagramType });

    const actionDispatcher: GLSPActionDispatcher = container.get<GLSPActionDispatcher>(TYPES.IActionDispatcher);

    actionDispatcher.dispatch(
        RequestModelAction.create({
            // Java's URLEncoder.encode encodes spaces as plus sign but decodeURI expects spaces to be encoded as %20.
            // See also https://en.wikipedia.org/wiki/Query_string#URL_encoding for URL encoding in forms vs generic URL encoding.
            options: {
                sourceUri: 'file://' + decodeURI(filePath.replace(/\+/g, '%20')),
                diagramType: 'workflow-diagram',
                isReconnecting
            }
        })
    );
    actionDispatcher.dispatch(RequestTypeHintsAction.create());
    await actionDispatcher.onceModelInitialized();
    actionDispatcher.dispatch(EnableToolPaletteAction.create());

    if (isReconnecting) {
        const message = `Connection to the ${id} glsp server got closed. Connection was successfully re-established.`;
        const timeout = 5000;
        const severity = 'WARNING';
        actionDispatcher.dispatchAll([
            ServerStatusAction.create(message, { severity, timeout }),
            ServerMessageAction.create(message, { severity })
        ]);
        return;
    }
}

async function reconnect(connectionProvider: MessageConnection): Promise<void> {
    container = createContainer(widgetId);
    diagramServer = container.get<GLSPDiagramServer>(TYPES.ModelSource);
    diagramServer.clientId = clientId;

    initialize(connectionProvider, true /* isReconnecting */);
}

function setWidgetId(mainWidgetId: string): void {
    const mainWidget = document.getElementById('sprotty');
    if (mainWidget) {
        mainWidget.id = mainWidgetId;
    }
}

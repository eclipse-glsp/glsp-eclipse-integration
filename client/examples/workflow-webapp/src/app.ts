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
    CenterAction,
    configureServerActions,
    EnableToolPaletteAction,
    GLSPActionDispatcher,
    GLSPDiagramServer,
    RequestModelAction,
    RequestTypeHintsAction,
    TYPES
} from '@eclipse-glsp/client';
import { getParameters } from '@eclipse-glsp/ide';
import { ApplicationIdProvider, GLSPClient, listen } from '@eclipse-glsp/protocol';
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
const container = createContainer(widgetId);

const diagramServer = container.get<GLSPDiagramServer>(TYPES.ModelSource);
diagramServer.clientId = clientId;

const websocket = new WebSocket(`ws://localhost:${port}/${id}`);
listen(websocket, connection => initialize(connection));

async function initialize(connectionProvider: MessageConnection): Promise<void> {
    const client = new BaseJsonrpcGLSPClient({ id, connectionProvider });
    await diagramServer.connect(client);
    const result = await client.initializeServer({
        applicationId,
        protocolVersion: GLSPClient.protocolVersion
    });
    await configureServerActions(result, diagramType, container);

    await client.initializeClientSession({ clientSessionId: diagramServer.clientId, diagramType });
    const actionDispatcher = container.get<GLSPActionDispatcher>(TYPES.IActionDispatcher);
    actionDispatcher.dispatch(
        RequestModelAction.create({
            // Java's URLEncoder.encode encodes spaces as plus sign but decodeURI expects spaces to be encoded as %20.
            // See also https://en.wikipedia.org/wiki/Query_string#URL_encoding for URL encoding in forms vs generic URL encoding.
            options: {
                sourceUri: 'file://' + decodeURI(filePath.replace(/\+/g, '%20')),
                diagramType: 'workflow-diagram'
            }
        })
    );
    actionDispatcher.dispatch(RequestTypeHintsAction.create());
    actionDispatcher.dispatch(EnableToolPaletteAction.create());
    actionDispatcher.onceModelInitialized().then(() => actionDispatcher.dispatch(CenterAction.create([])));
}

function setWidgetId(mainWidgetId: string): void {
    const mainWidget = document.getElementById('sprotty');
    if (mainWidget) {
        mainWidget.id = mainWidgetId;
    }
}

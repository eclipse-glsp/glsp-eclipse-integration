/********************************************************************************
 * Copyright (c) 2023 EclipseSource and others.
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

import { createWebSocketConnection, wrap } from '@eclipse-glsp/protocol/lib/client-server-protocol/jsonrpc/websocket-connection';
import { MaybePromise } from '@eclipse-glsp/protocol/lib/utils/type-util';
import { Logger, MessageConnection } from 'vscode-jsonrpc';

export interface GLSPWebSocketOptions {
    /**
     * Allow automatic reconnect of WebSocket connections
     * @default true
     */
    reconnecting?: boolean;
    /**
     * Max attempts of reconnects
     * @default Infinity
     */
    reconnectAttempts?: number;
    /**
     * The time delay in milliseconds between reconnect attempts
     * @default 1000
     */
    reconnectDelay?: number;
}

export const GLSPConnectionHandler = Symbol('GLSPConnectionHandler');
export interface GLSPConnectionHandler {
    onConnection(connection: MessageConnection): MaybePromise<void>;
    onReconnect(connection: MessageConnection): MaybePromise<void>;
    logger?: Logger;
}

export class GLSPWebSocketProvider {
    protected webSocket: WebSocket;
    protected reconnectTimer: NodeJS.Timer;
    protected interruptTimer: NodeJS.Timer;
    protected reconnectAttempts = 0;

    protected options: GLSPWebSocketOptions = {
        // default values
        reconnecting: true,
        reconnectAttempts: Infinity,
        reconnectDelay: 1000
    };

    constructor(protected url: string, options?: GLSPWebSocketOptions) {
        this.options = Object.assign(this.options, options);
        console.error(`GLSPWebSocketProvider initialized - opts: ${JSON.stringify(this.options)}`);
        // this.interruptTimer = setInterval(() => {
        //     // Allow 2 interrupts
        //     if (this.reconnectAttempts === 2 || this.webSocket.readyState >= WebSocket.CLOSING) {
        //         clearInterval(this.interruptTimer);
        //         return;
        //     }
        //     console.error('GLSPWebSocketProvider interrupting the socket now...');
        //     this.webSocket.close();
        //     console.error(
        //         `GLSPWebSocketProvider websocket ${this.webSocket.readyState === 2 ? 'CLOSING' : 'CLOSED'} ${this.webSocket.url}`
        //     );
        // }, 5000);
    }

    protected createWebSocket(url: string): WebSocket {
        return new WebSocket(url);
    }

    listen(handler: GLSPConnectionHandler, isReconnecting = false): Promise<void> {
        this.webSocket = this.createWebSocket(this.url);

        this.webSocket.onerror = (): void => {
            handler.logger?.error('GLSPWebSocketProvider Connection to server errored. Please make sure that the server is running!');
            clearInterval(this.reconnectTimer);
            clearInterval(this.interruptTimer);
            this.webSocket.close();
        };

        return new Promise(resolve => {
            this.webSocket.onopen = (): void => {
                clearInterval(this.reconnectTimer);
                const wrappedSocket = wrap(this.webSocket);
                const wsConnection = createWebSocketConnection(wrappedSocket, handler.logger);

                this.webSocket.onclose = (): void => {
                    const { reconnecting } = { ...this.options };
                    if (reconnecting) {
                        if (this.reconnectAttempts >= this.options.reconnectAttempts!) {
                            handler.logger?.error(
                                'GLSPWebSocketProvider WebSocket reconnect failed - maximum number reconnect attempts ' +
                                    `(${this.options.reconnectAttempts}) was exceeded!`
                            );
                        } else {
                            this.reconnectTimer = setInterval(() => {
                                handler.logger?.error('GLSPWebSocketProvider reconnecting...');
                                this.listen(handler, true);
                                this.reconnectAttempts++;
                            }, this.options.reconnectDelay!);
                        }
                    } else {
                        handler.logger?.error('GLSPWebSocketProvider WebSocket will not reconnect - closing the connection now!');
                    }
                };

                if (isReconnecting) {
                    handler.logger?.error('GLSPWebSocketProvider Reconnecting!');
                    handler.onReconnect(wsConnection);
                } else {
                    handler.logger?.error('GLSPWebSocketProvider Initializing!');
                    handler.onConnection(wsConnection);
                }
                resolve();
            };
        });
    }
}


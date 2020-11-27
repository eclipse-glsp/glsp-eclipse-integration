/********************************************************************************
 * Copyright (c) 2019-2020 EclipseSource and others.
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
import { Action, GLSPDiagramServer, ServerMessageAction, ServerStatusAction } from "@eclipse-glsp/client";
import { injectable } from "inversify";

@injectable()
export class EclipseGLSPDiagramServer extends GLSPDiagramServer {

    private isServerStatusAction(action: Action): action is ServerStatusAction {
        return ServerStatusAction.KIND === action.kind && 'severity' in action && 'message' in action;
    }

    handleLocally(action: Action): boolean {
        if (this.isServerStatusAction(action)) {
            return this.handleServerStatusAction(action);
        }
        return super.handleLocally(action);
    }

    protected handleServerStatusAction(action: ServerStatusAction): boolean {
        /* send to server */
        return true;
    }

    protected handleServerMessageAction(action: ServerMessageAction): boolean {
        /* send to server */
        return true;
    }

}

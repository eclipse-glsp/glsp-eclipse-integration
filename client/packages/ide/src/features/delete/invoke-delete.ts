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
import { Action, DeleteElementOperation, EditorContextService, IActionDispatcher, IActionHandler, TYPES } from '@eclipse-glsp/client';
import { inject, injectable } from 'inversify';

export class InvokeDeleteAction implements Action {
    static KIND = 'invoke-delete';
    readonly kind = InvokeDeleteAction.KIND;
}

export function isInvokeDeleteAction(action: Action): action is InvokeDeleteAction {
    return action.kind === InvokeDeleteAction.KIND;
}

@injectable()
export class InvokeDeleteActionHandler implements IActionHandler {
    @inject(TYPES.IActionDispatcher) protected actionDispatcher: IActionDispatcher;
    @inject(EditorContextService) protected editorContext: EditorContextService;

    handle(action: Action): void {
        if (isInvokeDeleteAction(action)) {
            this.handleDelete();
        }
    }

    handleDelete(): void {
        this.actionDispatcher.dispatch(DeleteElementOperation.create(this.editorContext.get().selectedElementIds));
    }
}

/********************************************************************************
 * Copyright (c) 2023-2024 EclipseSource and others.
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
    Action,
    EndProgressAction,
    FeatureModule,
    IActionHandler,
    ICommand,
    ILogger,
    StartProgressAction,
    TYPES,
    UpdateProgressAction,
    configureActionHandler
} from '@eclipse-glsp/client';
import { inject, injectable } from 'inversify';

export const eclipseFallbackModule = new FeatureModule(
    (bind, unbind, isBound, rebind) => {
        const context = { bind, unbind, isBound, rebind };
        bind(FallbackActionHandler).toSelf().inSingletonScope();
        configureActionHandler(context, StartProgressAction.KIND, FallbackActionHandler);
        configureActionHandler(context, UpdateProgressAction.KIND, FallbackActionHandler);
        configureActionHandler(context, EndProgressAction.KIND, FallbackActionHandler);
    },
    { featureId: Symbol('eclipseFallback') }
);

/**
 * A fallback action handler for actions sent by features that are currently not supported by
 * default in the eclipse context. Unhandled actions will be simply forwarded to the {@link ILogger}.
 */
@injectable()
export class FallbackActionHandler implements IActionHandler {
    @inject(TYPES.ILogger)
    protected logger: ILogger;

    handle(action: Action): void | Action | ICommand {
        this.logger.log(this, 'Unhandled action received:', action);
    }
}

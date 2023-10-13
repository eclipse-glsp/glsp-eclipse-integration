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

import { ModuleConfiguration, standaloneExportModule, statusModule } from '@eclipse-glsp/client';
import { eclipseCopyPasteModule } from './features/copy-paste/copy-paste-module';
import { eclipseDeleteModule } from './features/delete/delete-module';
import { eclipseFallbackModule } from './features/fallback/fallback-module';

export const ECLIPSE_DEFAULT_MODULES = [
    eclipseCopyPasteModule,
    eclipseDeleteModule,
    eclipseFallbackModule,
    standaloneExportModule
] as const;

export const ECLIPSE_DEFAULT_MODULE_CONFIG: ModuleConfiguration = { add: [...ECLIPSE_DEFAULT_MODULES], remove: statusModule };

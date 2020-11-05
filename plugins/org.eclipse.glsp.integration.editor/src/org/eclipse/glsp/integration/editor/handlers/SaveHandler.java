/********************************************************************************
 * Copyright (c) 2020 EclipseSource and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 ********************************************************************************/
package org.eclipse.glsp.integration.editor.handlers;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.api.action.kind.SaveModelAction;
import org.eclipse.ui.part.EditorPart;

/**
 * Optional Eclipse Handler for saving the current editor.
 * This handler is an alternative way to trigger the save,
 * without relying on the Eclipse EditorPart integration.
 *
 * If this handler is not used, then the {@link EditorPart#doSave}
 * API will be used instead.
 */
public class SaveHandler extends EclipseActionHandler {

   @Override
   protected void execute(final IEclipseContext context) {
      dispatchMessage(context, new SaveModelAction());
   }

}

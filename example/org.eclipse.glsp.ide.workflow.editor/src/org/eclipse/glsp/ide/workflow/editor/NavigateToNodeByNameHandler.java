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
package org.eclipse.glsp.ide.workflow.editor;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.handlers.EclipseActionHandler;
import org.eclipse.glsp.server.features.navigation.NavigateToTargetAction;
import org.eclipse.glsp.server.features.navigation.NavigationTarget;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.utils.ClientOptions;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;

public class NavigateToNodeByNameHandler extends EclipseActionHandler {

   @Override
   protected void execute(final IEclipseContext context) {
      Optional<GModelState> modelState = getModelState(context);
      if (modelState.isPresent()) {
         Shell shell = context.get(Shell.class);
         InputDialog dialog = new InputDialog(shell, "Task Name",
            "Specify Task Name (may not be the same as the label):", "", allOk -> null);
         if (dialog.open() == Window.OK) {
            Map<String, String> clientOptions = modelState.get().getClientOptions();
            String uri = ClientOptions.getValue(clientOptions, ClientOptions.SOURCE_URI).orElseThrow();
            NavigationTarget target = new NavigationTarget(uri, Collections.singletonMap("name", dialog.getValue()));
            NavigateToTargetAction action = new NavigateToTargetAction(target);
            dispatchMessage(context, action);
         }
      }
   }

}

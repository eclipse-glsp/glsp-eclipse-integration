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
package org.eclipse.glsp.integration.editor;

import javax.websocket.Session;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.integration.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.server.websocket.GLSPServerEndpoint;
import org.eclipse.ui.statushandlers.StatusManager;

public class DiagramWebsocketEndpoint extends GLSPServerEndpoint {

   @Override
   public void onError(final Session session, final Throwable throwable) {
      StatusManager.getManager().handle(
         new Status(IStatus.ERROR, GLSPEditorIntegrationPlugin.PLUGIN_ID, "Error in diagram web socket", throwable));
      super.onError(session, throwable);
   }

}

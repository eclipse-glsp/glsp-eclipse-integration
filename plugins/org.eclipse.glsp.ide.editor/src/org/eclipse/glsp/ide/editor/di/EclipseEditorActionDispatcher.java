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
package org.eclipse.glsp.ide.editor.di;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.eclipse.glsp.ide.editor.GLSPDiagramEditorPart;
import org.eclipse.glsp.ide.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ServerMessageAction;
import org.eclipse.glsp.server.actions.ServerStatusAction;
import org.eclipse.glsp.server.actions.SetDirtyStateAction;
import org.eclipse.glsp.server.features.contextactions.RequestContextActions;
import org.eclipse.glsp.server.internal.action.DefaultActionDispatcher;
import org.eclipse.glsp.server.protocol.ClientSessionManager;
import org.eclipse.glsp.server.protocol.GLSPServerException;

import com.google.inject.Inject;

public class EclipseEditorActionDispatcher extends DefaultActionDispatcher {

   @Inject
   public EclipseEditorActionDispatcher(final ClientSessionManager clientSessionManager) {
      super(clientSessionManager);
   }

   @Override
   protected List<CompletableFuture<Void>> runAction(final Action action, final String clientId) {
      if (!this.handleLocally(action, clientId)) {
         return super.runAction(action, clientId);
      }
      return Collections.emptyList();
   }

   protected boolean handleLocally(final Action action, final String clientId) {
      if (action instanceof ServerMessageAction) {
         return handleServerMessageAction((ServerMessageAction) action, clientId);
      } else if (action instanceof ServerStatusAction) {
         return handleServerStatusAction((ServerStatusAction) action, clientId);
      } else if (action instanceof SetDirtyStateAction) {
         return handleSetDirtyStateAction((SetDirtyStateAction) action, clientId);
      } else if (action instanceof RequestContextActions) {
         return handleRequestContextActions((RequestContextActions) action, clientId);
      }
      return false;
   }

   private boolean handleRequestContextActions(final RequestContextActions action, final String clientId) {
      getEditorPart(clientId).showContextMenu(action);
      return false;
   }

   protected boolean handleSetDirtyStateAction(final SetDirtyStateAction action, final String clientId) {
      getEditorPart(clientId).handle(action);
      return true;
   }

   protected boolean handleServerStatusAction(final ServerStatusAction action, final String clientId) {
      getEditorPart(clientId).showServerState(action);
      return true;
   }

   protected boolean handleServerMessageAction(final ServerMessageAction action, final String clientId) {
      // Do not process message notifications for now. Only rely on status actions
      return true;
   }

   protected GLSPDiagramEditorPart getEditorPart(final String clientId) {
      return GLSPServerException.getOrThrow(GLSPEditorIntegrationPlugin.getDefault().getGLSPEditorRegistry()
         .getGLSPEditor(clientId),
         "Could not retrieve GLSP Editor. GLSP editor is not properly configured for clientId: " + clientId);
   }

}

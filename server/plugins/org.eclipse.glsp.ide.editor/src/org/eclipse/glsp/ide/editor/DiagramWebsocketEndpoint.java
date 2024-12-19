/********************************************************************************
 * Copyright (c) 2020-2023 EclipseSource and others.
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
package org.eclipse.glsp.ide.editor;

import java.util.Collection;
import java.util.function.Function;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.glsp.server.protocol.GLSPClient;
import org.eclipse.glsp.server.websocket.GLSPServerEndpoint;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.ui.statushandlers.StatusManager;

import com.google.inject.Inject;

import jakarta.websocket.CloseReason;
import jakarta.websocket.Session;

public class DiagramWebsocketEndpoint extends GLSPServerEndpoint {

   protected GLSPClient glspClient;

   @Inject
   protected IdeGLSPClient ideGLSPClient;

   @Override
   public void onError(final Session session, final Throwable throwable) {
      StatusManager.getManager().handle(
         new Status(IStatus.ERROR, GLSPIdeEditorPlugin.PLUGIN_ID, "Error in diagram web socket", throwable));
      super.onError(session, throwable);
   }

   @Override
   protected Function<MessageConsumer, MessageConsumer> messageWrapper() {
      return (msg) -> new IdeMessageConsumer(ideGLSPClient, () -> glspClient, msg);
   }

   @Override
   public void onClose(final Session session, final CloseReason closeReason) {
      ideGLSPClient.disconnect(glspClient);
   }

   @Override
   protected void connect(final Collection<Object> localServices, final GLSPClient remoteProxy) {
      this.glspClient = remoteProxy;
      glspServer.connect(ideGLSPClient);

   }

}

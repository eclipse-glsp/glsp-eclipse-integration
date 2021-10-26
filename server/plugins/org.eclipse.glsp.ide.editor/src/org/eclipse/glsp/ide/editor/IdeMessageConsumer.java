/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
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

import java.util.function.Supplier;

import org.eclipse.glsp.server.protocol.GLSPClient;
import org.eclipse.glsp.server.protocol.GLSPServer;
import org.eclipse.glsp.server.protocol.InitializeClientSessionParameters;
import org.eclipse.lsp4j.jsonrpc.JsonRpcException;
import org.eclipse.lsp4j.jsonrpc.MessageConsumer;
import org.eclipse.lsp4j.jsonrpc.MessageIssueException;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;

/**
 * Custom message consumer that intercepts {@link GLSPServer#initializeClientSession(InitializeClientSessionParameters)}
 * requests to properly connect the session-specific client proxy to the {@link IdeGLSPClient}.
 *
 */
public class IdeMessageConsumer implements MessageConsumer {

   protected IdeGLSPClient ideGLSPClient;
   protected MessageConsumer delegate;
   protected Supplier<GLSPClient> clientProxySupplier;

   public IdeMessageConsumer(final IdeGLSPClient ideGLSPClient,
      final Supplier<GLSPClient> clientProxySupplier, final MessageConsumer delegate) {
      this.ideGLSPClient = ideGLSPClient;
      this.delegate = delegate;
      this.clientProxySupplier = clientProxySupplier;
   }

   @Override
   public void consume(final Message message) throws MessageIssueException, JsonRpcException {
      if (message instanceof RequestMessage) {
         handle((RequestMessage) message);
      }
      delegate.consume(message);

   }

   protected void handle(final RequestMessage message) {
      if (message.getMethod().equals("initializeClientSession")) {
         InitializeClientSessionParameters params = (InitializeClientSessionParameters) message.getParams();
         ideGLSPClient.connect(params.getClientSessionId(), clientProxySupplier.get());
      }

   }

}

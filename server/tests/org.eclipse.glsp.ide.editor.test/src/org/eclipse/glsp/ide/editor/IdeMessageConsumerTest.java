/********************************************************************************
 * Copyright (c) 2026 EclipseSource and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.glsp.ide.editor.IdeGLSPClientTest.RecordingClient;
import org.eclipse.glsp.server.actions.ActionMessage;
import org.eclipse.glsp.server.actions.CenterAction;
import org.eclipse.glsp.server.protocol.InitializeClientSessionParameters;
import org.eclipse.lsp4j.jsonrpc.messages.Message;
import org.eclipse.lsp4j.jsonrpc.messages.RequestMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdeMessageConsumerTest {

   private IdeGLSPClient ideClient;
   private RecordingClient proxy;
   private List<Message> delegated;
   private IdeMessageConsumer consumer;

   @BeforeEach
   void setUp() {
      ideClient = new IdeGLSPClient();
      proxy = new RecordingClient();
      delegated = new ArrayList<>();
      consumer = new IdeMessageConsumer(ideClient, () -> proxy, delegated::add);
   }

   @Test
   void connectsProxyOnSessionInitialization() {
      RequestMessage request = request("initializeClientSession",
         new InitializeClientSessionParameters("session", "workflow-diagram", List.of()));
      consumer.consume(request);

      ActionMessage message = new ActionMessage("session", new CenterAction());
      ideClient.process(message);

      assertEquals(List.of(message), proxy.getReceived());
      assertEquals(List.of(request), delegated);
   }

   @Test
   void forwardsOtherRequestsWithoutConnecting() {
      RequestMessage request = request("shutdown", null);
      consumer.consume(request);

      assertEquals(List.of(request), delegated);
      assertThrows(IllegalArgumentException.class,
         () -> ideClient.process(new ActionMessage("session", new CenterAction())));
   }

   static RequestMessage request(final String method, final Object params) {
      RequestMessage request = new RequestMessage();
      request.setId(1);
      request.setMethod(method);
      request.setParams(params);
      return request;
   }
}

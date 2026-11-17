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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.glsp.server.actions.ActionMessage;
import org.eclipse.glsp.server.actions.CenterAction;
import org.eclipse.glsp.server.protocol.GLSPClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IdeGLSPClientTest {

   private IdeGLSPClient client;

   @BeforeEach
   void setUp() {
      client = new IdeGLSPClient();
   }

   @Test
   void forwardsMessagesToAllProxiesOfTheSession() {
      RecordingClient first = new RecordingClient();
      RecordingClient second = new RecordingClient();
      RecordingClient other = new RecordingClient();
      client.connect("session", first);
      client.connect("session", second);
      client.connect("other", other);

      ActionMessage message = message("session");
      client.process(message);

      assertEquals(List.of(message), first.getReceived());
      assertEquals(List.of(message), second.getReceived());
      assertTrue(other.getReceived().isEmpty());
   }

   @Test
   void rejectsMessagesForUnknownSessions() {
      assertThrows(IllegalArgumentException.class, () -> client.process(message("unknown")));
   }

   @Test
   void disconnectsProxyFromSession() {
      RecordingClient proxy = new RecordingClient();
      client.connect("session", proxy);

      assertTrue(client.disconnect("session", proxy));
      assertFalse(client.disconnect("session", proxy));
      assertThrows(IllegalArgumentException.class, () -> client.process(message("session")));
   }

   @Test
   void keepsSessionWhileOtherProxiesAreConnected() {
      RecordingClient first = new RecordingClient();
      RecordingClient second = new RecordingClient();
      client.connect("session", first);
      client.connect("session", second);

      client.disconnect("session", first);
      client.process(message("session"));

      assertTrue(first.getReceived().isEmpty());
      assertEquals(1, second.getReceived().size());
   }

   @Test
   void disconnectsProxyFromAllSessions() {
      RecordingClient proxy = new RecordingClient();
      client.connect("first", proxy);
      client.connect("second", proxy);

      client.disconnect(proxy);

      assertThrows(IllegalArgumentException.class, () -> client.process(message("first")));
      assertThrows(IllegalArgumentException.class, () -> client.process(message("second")));
   }

   static ActionMessage message(final String clientId) {
      return new ActionMessage(clientId, new CenterAction());
   }

   static final class RecordingClient implements GLSPClient {
      private final List<ActionMessage> received = new ArrayList<>();

      @Override
      public void process(final ActionMessage message) {
         received.add(message);
      }

      List<ActionMessage> getReceived() { return received; }
   }
}

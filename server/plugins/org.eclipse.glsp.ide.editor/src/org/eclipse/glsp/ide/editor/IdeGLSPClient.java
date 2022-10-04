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

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.eclipse.glsp.server.actions.ActionMessage;
import org.eclipse.glsp.server.protocol.GLSPClient;
import org.eclipse.glsp.server.protocol.GLSPServer;
import org.eclipse.glsp.server.session.ClientSessionManager;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.inject.Inject;

/**
 * A {@link GLSPClient} facade implementation that handles multiple {@link GLSPClient} proxies (one for each client
 * session).This can be connected to the {@link GLSPServer} if a dedicated (web)socket connection for each client
 * session is
 * used (Eclipse Integration). Session-specific {@link GLSPClient} proxies can be connected with the
 * {@link IdeGLSPClient#connect(String, GLSPClient)} method.
 */
public class IdeGLSPClient implements GLSPClient {

   @Inject
   protected GLSPServer glspServer;

   @Inject
   protected ClientSessionManager clientSessionManager;

   protected Multimap<String, GLSPClient> clientProxies;

   public IdeGLSPClient() {
      clientProxies = HashMultimap.create();
   }

   /**
    * Connect a {@link GLSPClient} proxy for the given client session id. All action messages with matching client id
    * that are received via {@link GLSPClient#process(ActionMessage)} will be delegated to this proxy.
    *
    * @param clientSessionId The id of the client session
    * @param glspClient      The GLSPClient proxy given client id). `false` otherwise.
    */
   public void connect(final String clientSessionId, final GLSPClient glspClient) {
      clientProxies.put(clientSessionId, glspClient);
   }

   /**
    * Disconnects the registered {@link GLSPClient} proxy for the given client session id.
    *
    * @param clientSessionId The client session id of the proxy that should disconnected.
    * @param glspClient The glsp client proxy that should be disconnected.
    * @return `true` if a proxy for the given id was connected and has been successfully disconnected. `false`
    *         otherwise.
    */
   public boolean disconnect(final String clientSessionId, final GLSPClient glspClient) {
      var result = clientProxies.remove(clientSessionId, glspClient);
      if (clientProxies.containsKey(clientSessionId)) {
         return false;
      }
      clientSessionManager.disposeClientSession(clientSessionId);
      return result;
   }

   /**
    * Disconnect the given {@link GLSPClient} proxy. If the proxy has been connected with multiple different client
    * session ids it is removed for all sessions.
    *
    * @param glspClient The glsp client proxy that should be disconnected.
    */
   public void disconnect(final GLSPClient glspClient) {
      clientProxies.entries().stream()
         .filter(entry -> entry.getValue() == glspClient)
         .map(Entry::getKey)
         .forEach(id -> disconnect(id, glspClient));
   }

   @Override
   public void process(final ActionMessage message) {
      Collection<GLSPClient> result = clientProxies.get(message.getClientId());
      Preconditions.checkState(!result.isEmpty(),
         "Could not retrieve GLSPCLient proxy for client session with id: " + message.getClientId());
      result.forEach(proxy -> proxy.process(message));
   }

}

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

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.log4j.BasicConfigurator;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.glsp.server.di.GLSPModule;
import org.eclipse.glsp.server.websocket.GLSPConfigurator;
import org.eclipse.glsp.server.websocket.GLSPServerEndpoint;
import org.eclipse.glsp.server.websocket.WebsocketModule;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import com.google.inject.Guice;
import com.google.inject.Injector;

public abstract class GLSPServerManager {

   protected Server server;
   protected ServerContainer container;
   protected Injector injector;

   public synchronized void start() throws Exception {
      if (server == null || !server.isRunning()) {
         server = new Server(new InetSocketAddress("localhost", 8081));

         configure(server);
         server.start();
      }
   }

   public abstract String getId();

   public abstract GLSPModule getModule();

   public Injector getInjector() { return injector; }

   public abstract URL getResourceURL();

   @SuppressWarnings("checkstyle:ThrowsCount")
   protected void configure(final Server server)
      throws URISyntaxException, IOException, ServletException, DeploymentException {
      BasicConfigurator.configure();
      injector = Guice.createInjector(getModule(), new WebsocketModule());

      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);

      ServletHolder defaultServletHolder = new ServletHolder("default", new DefaultServlet());

      String resourceBase = new File(FileLocator.resolve(getResourceURL()).toURI()).getAbsolutePath();
      defaultServletHolder.setInitParameter("resourceBase", resourceBase);
      defaultServletHolder.setInitParameter("dirAllowed", "false");
      context.addServlet(defaultServletHolder, "/");

      container = WebSocketServerContainerInitializer.configureContext(context);
      ServerEndpointConfig.Builder builder = ServerEndpointConfig.Builder.create(GLSPServerEndpoint.class,
         "/" + getId());
      builder.configurator(new GLSPConfigurator(injector));
      container.addEndpoint(builder.build());

   }

   @SuppressWarnings("checkstyle:illegalCatch")
   public synchronized void stop() {
      if (server != null) {
         try {
            server.stop();
         } catch (Exception e) {
            // Ignore
         }
      }
   }

   public Optional<Session> getSessionFor(final String clientId) {
      return container.getOpenSessions().stream().filter(s -> s.getUserProperties().get("clientId") != null
         && s.getUserProperties().get("clientId").equals(clientId)).findFirst();
   }

   public Server getServer() { return server; }
}

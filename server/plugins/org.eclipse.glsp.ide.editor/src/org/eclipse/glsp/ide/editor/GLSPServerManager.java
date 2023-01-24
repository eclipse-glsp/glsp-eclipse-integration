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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpointConfig;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.builder.api.AppenderComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilder;
import org.apache.logging.log4j.core.config.builder.api.ConfigurationBuilderFactory;
import org.apache.logging.log4j.core.config.builder.api.LayoutComponentBuilder;
import org.apache.logging.log4j.core.config.builder.api.RootLoggerComponentBuilder;
import org.apache.logging.log4j.core.config.builder.impl.BuiltConfiguration;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.glsp.ide.editor.internal.utils.SystemUtils;
import org.eclipse.glsp.server.di.ServerModule;
import org.eclipse.glsp.server.utils.LaunchUtil;
import org.eclipse.glsp.server.websocket.GLSPConfigurator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.javax.server.config.JavaxWebSocketServletContainerInitializer;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

public abstract class GLSPServerManager {

   protected Server server;
   protected ServerContainer container;
   protected Injector injector;
   protected int localPort;

   public synchronized void start() throws Exception {
      if (server == null || !server.isRunning()) {
         server = new Server(new InetSocketAddress("localhost", 0));

         configure(server);
         server.start();

         // Use any available port, as several Eclipse instances (Or several subclasses
         // of GLSPServerManager) may be started at the same time.
         this.localPort = Arrays.stream(server.getConnectors()).findFirst()
            .map(ServerConnector.class::cast).map(ServerConnector::getLocalPort).orElse(-1);
      }
   }

   public abstract String getGlspId();

   public abstract URL getResourceURL();

   /**
    * A hook to setup services e.g. logging right before the server is initialized.
    */
   protected void preConfigure() {
      // can be overwritten by subclasses
   }

   @SuppressWarnings({ "checkstyle:ThrowsCount" })
   protected void configure(final Server server)
      throws URISyntaxException, IOException, ServletException, DeploymentException {

      preConfigure();
      ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
      context.setContextPath("/");
      server.setHandler(context);

      ServletHolder defaultServletHolder = new ServletHolder("default", new DefaultServlet());

      String resourceBase = resolveResourceBase();

      defaultServletHolder.setInitParameter("resourceBase", resourceBase);
      defaultServletHolder.setInitParameter("dirAllowed", "false");
      context.addServlet(defaultServletHolder, "/");
      JavaxWebSocketServletContainerInitializer.configure(context, (servletContext, wsContainer) -> {
         container = wsContainer;
         ServerEndpointConfig.Builder builder = ServerEndpointConfig.Builder.create(DiagramWebsocketEndpoint.class,
            "/" + getGlspId());
         Injector injector = createInjector();
         builder.configurator(new GLSPConfigurator(() -> injector));
         wsContainer.addEndpoint(builder.build());
         wsContainer.setDefaultMaxSessionIdleTimeout(-1);
      });
   }

   protected abstract ServerModule configureServerModule();

   protected List<Module> configureAdditionalModules() {
      return Collections.emptyList();
   }

   protected Injector createInjector() {
      List<Module> modules = new ArrayList<>();
      modules.add(configureServerModule());
      configureAdditionalModules().forEach(modules::add);
      return Guice.createInjector(modules);
   }

   protected String resolveResourceBase() throws IOException {
      String resourceBase = FileLocator.resolve(getResourceURL()).getFile();
      if (SystemUtils.isWindows() && resourceBase.startsWith("/")) {
         // for some reason paths may start with '/' on Windows, e.g., '/C:/...'
         resourceBase = resourceBase.substring(1);
      }
      return resourceBase;
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

   public Server getServer() { return server; }

   /**
    * Return the port used by this server. Ports are computed dynamically, as multiple Eclipse IDEs
    * may be running in parallel, each with their own embedded server(s).
    *
    * @return
    *         the port used by this server.
    */
   public int getLocalPort() { return this.localPort; }

   public static void configureLogger(final Level level) {

      LaunchUtil.configureLogger(true, level);

      ConfigurationBuilder<BuiltConfiguration> builder = ConfigurationBuilderFactory.newConfigurationBuilder();

      // Configure new root logger
      RootLoggerComponentBuilder rootLogger = builder.newRootLogger(level);

      // Configure pattern layout
      LayoutComponentBuilder patternLayout = builder.newLayout("PatternLayout")
         .addAttribute("pattern", "%d{DEFAULT_NANOS} [%t] %-5level %logger{1} - %msg%n");

      // Configure console logging
      AppenderComponentBuilder consoleAppender = builder.newAppender("ConsoleLogger", "CONSOLE")
         .addAttribute("target", ConsoleAppender.Target.SYSTEM_OUT)
         .add(patternLayout);
      builder.add(consoleAppender);
      rootLogger.add(builder.newAppenderRef("ConsoleLogger"));

      // Add root logger and reconfigure to use created configuration
      builder.add(rootLogger);
      Configurator.reconfigure(builder.build());
   }
}

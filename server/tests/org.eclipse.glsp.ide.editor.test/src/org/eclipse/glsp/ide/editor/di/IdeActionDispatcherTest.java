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
package org.eclipse.glsp.ide.editor.di;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GraphFactory;
import org.eclipse.glsp.ide.editor.actions.InitializeCanvasBoundsAction;
import org.eclipse.glsp.ide.editor.initialization.DefaultModelInitializationConstraint;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.actions.ClientActionForwarder;
import org.eclipse.glsp.server.features.core.model.RequestModelAction;
import org.eclipse.glsp.server.features.core.model.SetModelAction;
import org.eclipse.glsp.server.internal.actions.DefaultActionHandlerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("restriction")
class IdeActionDispatcherTest {

   private static final long AWAIT_MS = 1_000L;

   private TestDispatcher dispatcher;

   @BeforeEach
   void setUp() {
      dispatcher = new TestDispatcher();
   }

   @AfterEach
   void tearDown() {
      dispatcher.dispose();
   }

   @Test
   void initializesWhenSetModelAnswersModelRequest()
      throws InterruptedException, ExecutionException, TimeoutException {
      // The SetModelAction resolves the pending request and therefore never reaches runAction.
      SetModelAction response = dispatcher.request(new RequestModelAction()).get(AWAIT_MS, MILLISECONDS);
      assertNotNull(response.getNewRoot());
      assertFalse(dispatcher.onceModelInitialized().isDone());

      dispatcher.dispatch(new InitializeCanvasBoundsAction()).get(AWAIT_MS, MILLISECONDS);

      dispatcher.onceModelInitialized().get(AWAIT_MS, MILLISECONDS);
   }

   @Test
   void initializesWhenSetModelIsDispatchedDirectly()
      throws InterruptedException, ExecutionException, TimeoutException {
      dispatcher.dispatch(new SetModelAction(createGraph())).get(AWAIT_MS, MILLISECONDS);
      assertFalse(dispatcher.onceModelInitialized().isDone());

      dispatcher.dispatch(new InitializeCanvasBoundsAction()).get(AWAIT_MS, MILLISECONDS);

      dispatcher.onceModelInitialized().get(AWAIT_MS, MILLISECONDS);
   }

   @Test
   void doesNotInitializeWithoutModel() throws InterruptedException, ExecutionException, TimeoutException {
      dispatcher.dispatch(new InitializeCanvasBoundsAction()).get(AWAIT_MS, MILLISECONDS);

      assertFalse(dispatcher.onceModelInitialized().isDone());
   }

   static GGraph createGraph() {
      GGraph graph = GraphFactory.eINSTANCE.createGGraph();
      graph.setId("graph");
      graph.setType("graph");
      return graph;
   }

   /** Dispatcher wired with stubs instead of the Guice-injected handler registry and client forwarder. */
   static final class TestDispatcher extends IdeActionDispatcher {
      TestDispatcher() {
         super("test", new DefaultModelInitializationConstraint());
         this.actionHandlerRegistry = new DefaultActionHandlerRegistry(Set.of(
            new StubHandler(RequestModelAction.class, action -> List.of(new SetModelAction(createGraph()))),
            new StubHandler(InitializeCanvasBoundsAction.class, action -> List.of())));
         this.clientActionForwarder = new StubForwarder();
      }
   }

   /** Claims SetModelActions as forwarded to the client, like the real forwarder, without a client connection. */
   static final class StubForwarder extends ClientActionForwarder {
      @Override
      public boolean handle(final Action action) {
         return action instanceof SetModelAction;
      }
   }

   static final class StubHandler implements ActionHandler {
      private final Class<? extends Action> handledType;
      private final Function<Action, List<Action>> execute;

      StubHandler(final Class<? extends Action> handledType, final Function<Action, List<Action>> execute) {
         this.handledType = handledType;
         this.execute = execute;
      }

      @Override
      public List<Class<? extends Action>> getHandledActionTypes() { return List.of(handledType); }

      @Override
      public List<Action> execute(final Action action) {
         return execute.apply(action);
      }
   }
}

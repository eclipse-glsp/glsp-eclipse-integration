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
package org.eclipse.glsp.ide.editor.initialization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.glsp.graph.GGraph;
import org.eclipse.glsp.graph.GraphFactory;
import org.eclipse.glsp.ide.editor.actions.InitializeCanvasBoundsAction;
import org.eclipse.glsp.server.features.core.model.SetModelAction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DefaultModelInitializationConstraintTest {

   private DefaultModelInitializationConstraint constraint;

   @BeforeEach
   void setUp() {
      constraint = new DefaultModelInitializationConstraint();
   }

   @Test
   void completesAfterModelAndCanvasBounds() {
      constraint.notifyDispatched(new SetModelAction(createGraph("graph")));
      assertFalse(constraint.isCompleted());

      constraint.notifyDispatched(new InitializeCanvasBoundsAction());

      assertTrue(constraint.isCompleted());
      assertTrue(constraint.onInitialized().isDone());
   }

   @Test
   void ignoresCanvasBoundsBeforeModel() {
      constraint.notifyDispatched(new InitializeCanvasBoundsAction());
      constraint.notifyDispatched(new SetModelAction(createGraph("graph")));

      assertFalse(constraint.isCompleted());
   }

   @Test
   void ignoresEmptyModel() {
      constraint.notifyDispatched(new SetModelAction(createGraph("NONE")));
      constraint.notifyDispatched(new InitializeCanvasBoundsAction());

      assertFalse(constraint.isCompleted());
   }

   @Test
   void ignoresModelWithoutRoot() {
      SetModelAction action = new SetModelAction();
      constraint.notifyDispatched(action);
      constraint.notifyDispatched(new InitializeCanvasBoundsAction());

      assertFalse(constraint.isCompleted());
   }

   static GGraph createGraph(final String type) {
      GGraph graph = GraphFactory.eINSTANCE.createGGraph();
      graph.setId("graph");
      graph.setType(type);
      return graph;
   }
}

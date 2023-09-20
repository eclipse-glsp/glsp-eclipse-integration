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
package org.eclipse.glsp.ide.workflow.editor;

import org.eclipse.glsp.example.workflow.WorkflowDiagramModule;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeMessageActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeNavigateToExternalTargetActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSelectActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSelectAllActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSetDirtyStateActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSetMarkersActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeStatusActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.InitializeCanvasBoundsActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.SetClipboardDataActionHandler;
import org.eclipse.glsp.ide.editor.di.IdeActionDispatcher;
import org.eclipse.glsp.ide.editor.gmodel.operations.IdeGModelPasteOperationHandler;
import org.eclipse.glsp.ide.editor.initialization.DefaultModelInitializationConstraint;
import org.eclipse.glsp.ide.editor.initialization.ModelInitializationConstraint;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.di.MultiBinding;
import org.eclipse.glsp.server.gmodel.GModelPasteOperationHandler;
import org.eclipse.glsp.server.operations.OperationHandler;

import com.google.inject.Scopes;

class WorkflowGLSPEclipseModule extends WorkflowDiagramModule {
   @Override
   public void configure() {
      super.configure();
      bind(ModelInitializationConstraint.class).to(DefaultModelInitializationConstraint.class).in(Scopes.SINGLETON);
   }

   @Override
   protected Class<? extends ActionDispatcher> bindActionDispatcher() {
      return IdeActionDispatcher.class;
   }

   @Override
   protected void configureActionHandlers(final MultiBinding<ActionHandler> bindings) {
      super.configureActionHandlers(bindings);
      bindings.add(SetClipboardDataActionHandler.class);
      bindings.add(IdeSetMarkersActionHandler.class);
      bindings.add(IdeNavigateToExternalTargetActionHandler.class);
      bindings.add(IdeMessageActionHandler.class);
      bindings.add(IdeSetDirtyStateActionHandler.class);
      bindings.add(IdeStatusActionHandler.class);
      bindings.add(InitializeCanvasBoundsActionHandler.class);
      bindings.add(IdeSelectActionHandler.class);
      bindings.add(IdeSelectAllActionHandler.class);
   }

   @Override
   protected void configureOperationHandlers(final MultiBinding<OperationHandler<?>> bindings) {
      super.configureOperationHandlers(bindings);
      bindings.remove(GModelPasteOperationHandler.class);
      bindings.add(IdeGModelPasteOperationHandler.class);
   }

}

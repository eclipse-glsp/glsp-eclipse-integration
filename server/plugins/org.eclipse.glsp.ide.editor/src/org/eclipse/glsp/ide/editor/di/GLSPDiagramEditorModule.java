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
package org.eclipse.glsp.ide.editor.di;

import javax.websocket.Endpoint;

import org.eclipse.glsp.ide.editor.actions.InvokeCopyAction;
import org.eclipse.glsp.ide.editor.actions.InvokeCutAction;
import org.eclipse.glsp.ide.editor.actions.InvokeDeleteAction;
import org.eclipse.glsp.ide.editor.actions.InvokePasteAction;
import org.eclipse.glsp.ide.editor.actions.NavigateAction;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeInitializeClientSessionActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeNavigateToExternalTargetActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSaveModelActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeServerMessageActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeServerStatusActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSetDirtyStateActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.IdeSetMarkersActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.InitializeCanvasBoundsActionHandler;
import org.eclipse.glsp.ide.editor.actions.handlers.SetClipboardDataActionHandler;
import org.eclipse.glsp.ide.editor.clipboard.ClipboardService;
import org.eclipse.glsp.ide.editor.clipboard.ui.DisplayClipboardService;
import org.eclipse.glsp.ide.editor.initialization.DefaultModelInitializationConstraint;
import org.eclipse.glsp.ide.editor.initialization.ModelInitializationConstraint;
import org.eclipse.glsp.ide.editor.operations.handlers.EclipsePasteOperationHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.actions.GLSPServerStatusAction;
import org.eclipse.glsp.server.actions.InitializeClientSessionActionHandler;
import org.eclipse.glsp.server.actions.SaveModelActionHandler;
import org.eclipse.glsp.server.actions.ServerMessageAction;
import org.eclipse.glsp.server.actions.ServerStatusAction;
import org.eclipse.glsp.server.actions.SetDirtyStateAction;
import org.eclipse.glsp.server.di.DefaultGLSPModule;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.features.core.model.ModelFactory;
import org.eclipse.glsp.server.features.navigation.NavigateToExternalTargetAction;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.operations.gmodel.PasteOperationHandler;
import org.eclipse.glsp.server.utils.MultiBinding;
import org.eclipse.glsp.server.websocket.GLSPServerEndpoint;

import com.google.inject.Scopes;

// FIXME: This module is not actually used, because we can't combine it with language-specific GLSPModules
// It should not extend DefaultGLSPModule and should only specify new Actions/Handlers and overridden
// services.
// Currently, Eclipse specializations of the Language modules need to copy everything from this module.
public class GLSPDiagramEditorModule extends DefaultGLSPModule {

   @Override
   public void configure() {
      super.configure();
      bind(Endpoint.class).to(GLSPServerEndpoint.class);
      bind(ClipboardService.class).to(DisplayClipboardService.class);
      bind(ModelInitializationConstraint.class).to(DefaultModelInitializationConstraint.class).in(Scopes.SINGLETON);
   }

   @Override
   protected Class<? extends ActionDispatcher> bindActionDispatcher() {
      return EclipseEditorActionDispatcher.class;
   }

   @Override
   protected void configureActionHandlers(final MultiBinding<ActionHandler> bindings) {
      super.configureActionHandlers(bindings);
      bindings.add(SetClipboardDataActionHandler.class);
      bindings.add(IdeSetMarkersActionHandler.class);
      bindings.add(IdeNavigateToExternalTargetActionHandler.class);
      bindings.add(IdeServerMessageActionHandler.class);
      bindings.add(IdeSetDirtyStateActionHandler.class);
      bindings.add(IdeServerStatusActionHandler.class);
      bindings.add(InitializeCanvasBoundsActionHandler.class);
      bindings.rebind(SaveModelActionHandler.class, IdeSaveModelActionHandler.class);
      bindings.rebind(InitializeClientSessionActionHandler.class, IdeInitializeClientSessionActionHandler.class);
   }

   @Override
   protected void configureOperationHandlers(final MultiBinding<OperationHandler> bindings) {
      super.configureOperationHandlers(bindings);
      bindings.remove(PasteOperationHandler.class);
      bindings.add(EclipsePasteOperationHandler.class);
   }

   @Override
   protected void configureClientActions(final MultiBinding<Action> bindings) {
      super.configureClientActions(bindings);
      bindings.add(InvokeCopyAction.class);
      bindings.add(InvokeCutAction.class);
      bindings.add(InvokePasteAction.class);
      bindings.add(InvokeDeleteAction.class);

      bindings.add(NavigateAction.class);

      bindings.remove(NavigateToExternalTargetAction.class);
      bindings.remove(ServerMessageAction.class);
      bindings.remove(SetDirtyStateAction.class);
      bindings.remove(ServerStatusAction.class);
      bindings.remove(GLSPServerStatusAction.class);
   }

   @Override
   protected void configureDiagramConfigurations(final MultiBinding<DiagramConfiguration> binding) {

   }

   @Override
   protected Class<? extends ModelFactory> bindModelFactory() {
      return ModelFactory.NullImpl.class; // Specified by language-specific modules
   }

}

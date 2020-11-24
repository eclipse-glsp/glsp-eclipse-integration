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
import org.eclipse.glsp.ide.editor.actions.InvokePasteAction;
import org.eclipse.glsp.ide.editor.actions.handlers.SetClipboardDataActionHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.ActionHandler;
import org.eclipse.glsp.server.di.DefaultGLSPModule;
import org.eclipse.glsp.server.diagram.DiagramConfiguration;
import org.eclipse.glsp.server.features.core.model.ModelFactory;
import org.eclipse.glsp.server.utils.MultiBinding;
import org.eclipse.glsp.server.websocket.GLSPServerEndpoint;

// FIXME: This module is not actually used, because we can't combine it with language-specific GLSPModules
// It should not extend DefaultGLSPModule and should only specify new Actions/Handlers and overridden
// services.
// Currently, Eclipse specializations of the Language modules need to copy everything from this module.
public class EclipseEditorGLSPModule extends DefaultGLSPModule {

   @Override
   public void configure() {
      super.configure();
      bind(Endpoint.class).to(GLSPServerEndpoint.class);
   }

   @Override
   protected Class<? extends ActionDispatcher> bindActionDispatcher() {
      return EclipseEditorActionDispatcher.class;
   }

   @Override
   protected void configureActionHandlers(final MultiBinding<ActionHandler> bindings) {
      super.configureActionHandlers(bindings);
      bindings.add(SetClipboardDataActionHandler.class);
   }

   @Override
   protected void configureClientActions(final MultiBinding<Action> bindings) {
      super.configureClientActions(bindings);
      bindings.add(InvokeCopyAction.class);
      bindings.add(InvokeCutAction.class);
      bindings.add(InvokePasteAction.class);
   }

   @Override
   protected void configureDiagramConfigurations(final MultiBinding<DiagramConfiguration> binding) {

   }

   @Override
   protected Class<? extends ModelFactory> bindModelFactory() {
      return ModelFactory.NullImpl.class; // Specified by language-specific modules
   }

}

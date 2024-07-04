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
package org.eclipse.glsp.ide.editor.di;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.glsp.ide.editor.initialization.ModelInitializationConstraint;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.di.ClientId;
import org.eclipse.glsp.server.features.contextactions.RequestContextActions;
import org.eclipse.glsp.server.internal.actions.DefaultActionDispatcher;

import com.google.inject.Inject;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class IdeActionDispatcher extends DefaultActionDispatcher {
   protected static final Logger LOGGER = LogManager.getLogger(IdeActionDispatcher.class);

   protected final CompletableFuture<Void> onModelInitialized;

   protected final ModelInitializationConstraint initializationConstraint;

   @Inject
   public IdeActionDispatcher(@ClientId() final String clientId,
      final ModelInitializationConstraint initializationConstraint) {
      super();
      this.clientId = clientId;
      this.initializationConstraint = initializationConstraint;
      this.onModelInitialized = initializationConstraint.onInitialized();
      this.onModelInitialized.thenRun(() -> LOGGER.info("Model Initialized."));
   }

   @Inject
   public void initInjector(final Injector injector) {
      GLSPIdeEditorPlugin.getDefaultGLSPEditorRegistry().getGLSPEditor(clientId)
         .ifPresent(editor -> editor.setInjector(injector));
   }

   public CompletableFuture<Void> onceModelInitialized() {
      return this.onModelInitialized;
   }

   @Override
   protected List<CompletableFuture<Void>> runAction(final Action action) {
      if (!this.handleLocally(action, clientId)) {
         List<CompletableFuture<Void>> actions = super.runAction(action);
         this.initializationConstraint.notifyDispatched(action);
         return actions;
      }
      return Collections.emptyList();
   }

   protected boolean handleLocally(final Action action, final String clientId) {
      if (action instanceof RequestContextActions) {
         return handleRequestContextActions((RequestContextActions) action, clientId);
      }
      return false;
   }

   protected boolean handleRequestContextActions(final RequestContextActions action, final String clientId) {
      GLSPIdeEditorPlugin.getDefaultGLSPEditorRegistry().getGLSPEditorOrThrow(clientId)
         .handleRequestContext(action);
      return false;
   }
}

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

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.eclipse.glsp.ide.editor.initialization.ModelInitializationConstraint;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.features.contextactions.RequestContextActions;
import org.eclipse.glsp.server.internal.action.DefaultActionDispatcher;
import org.eclipse.glsp.server.protocol.ClientSessionManager;

import com.google.inject.Inject;

@SuppressWarnings("restriction")
public class EclipseEditorActionDispatcher extends DefaultActionDispatcher {
   private static final Logger LOGGER = Logger.getLogger(EclipseEditorActionDispatcher.class);

   private final CompletableFuture<Void> onModelInitialized;

   private final ModelInitializationConstraint initializationConstraint;

   @Inject
   public EclipseEditorActionDispatcher(final ClientSessionManager clientSessionManager,
      final ModelInitializationConstraint initializationConstraint) {
      super(clientSessionManager);
      this.initializationConstraint = initializationConstraint;
      this.onModelInitialized = initializationConstraint.onInitialized();
      this.onModelInitialized.thenRun(() -> LOGGER.info("Model Initialized."));
   }

   public CompletableFuture<Void> onceModelInitialized() {
      return this.onModelInitialized;
   }

   @Override
   protected List<CompletableFuture<Void>> runAction(final Action action, final String clientId) {
      if (!this.handleLocally(action, clientId)) {
         List<CompletableFuture<Void>> actions = super.runAction(action, clientId);
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

   private boolean handleRequestContextActions(final RequestContextActions action, final String clientId) {
      GLSPIdeEditorPlugin.getDefaultGLSPEditorRegistry().getGLSPEditorOrThrow(clientId)
         .handleRequestContext(action);
      return false;
   }

}

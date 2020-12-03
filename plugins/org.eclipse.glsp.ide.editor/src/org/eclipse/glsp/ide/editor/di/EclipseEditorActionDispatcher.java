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
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.ide.editor.initialization.ModelInitializationConstraint;
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramEditorPart;
import org.eclipse.glsp.ide.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.InitializeClientSessionAction;
import org.eclipse.glsp.server.actions.ServerMessageAction;
import org.eclipse.glsp.server.actions.ServerStatusAction;
import org.eclipse.glsp.server.actions.SetDirtyStateAction;
import org.eclipse.glsp.server.features.contextactions.RequestContextActions;
import org.eclipse.glsp.server.internal.action.DefaultActionDispatcher;
import org.eclipse.glsp.server.protocol.ClientSessionManager;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.types.Severity;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Display;

import com.google.inject.Inject;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class EclipseEditorActionDispatcher extends DefaultActionDispatcher {
   private static final Logger LOGGER = Logger.getLogger(EclipseEditorActionDispatcher.class);

   @Inject
   private Injector injector;

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
      if (action instanceof InitializeClientSessionAction) {
         return handleInitializeClientSession((InitializeClientSessionAction) action, clientId);
      } else if (action instanceof ServerMessageAction) {
         return handleServerMessageAction((ServerMessageAction) action, clientId);
      } else if (action instanceof ServerStatusAction) {
         return handleServerStatusAction((ServerStatusAction) action, clientId);
      } else if (action instanceof SetDirtyStateAction) {
         return handleSetDirtyStateAction((SetDirtyStateAction) action, clientId);
      } else if (action instanceof RequestContextActions) {
         return handleRequestContextActions((RequestContextActions) action, clientId);
      }
      return false;
   }

   private boolean handleInitializeClientSession(final InitializeClientSessionAction action, final String clientId) {
      // Associate the injection context with the EditorPart (which is created before the Injector; so
      // it needs to know about it).
      getEditorPart(clientId).setInjector(injector);
      return false;
   }

   private boolean handleRequestContextActions(final RequestContextActions action, final String clientId) {
      getEditorPart(clientId).showContextMenu(action);
      return false;
   }

   protected boolean handleSetDirtyStateAction(final SetDirtyStateAction action, final String clientId) {
      getEditorPart(clientId).handle(action);
      return true;
   }

   protected boolean handleServerStatusAction(final ServerStatusAction action, final String clientId) {
      getEditorPart(clientId).showServerState(action);
      return true;
   }

   protected boolean handleServerMessageAction(final ServerMessageAction action, final String clientId) {
      toStatus(action).ifPresent(s -> log(s, action, clientId));
      return true;
   }

   private void log(final IStatus status, final ServerMessageAction action, final String clientId) {
      Platform.getLog(getClass()).log(status);
      if (status.getSeverity() >= IStatus.ERROR) {
         Display.getDefault().asyncExec(() -> {
            ErrorDialog.openError(getEditorPart(clientId).getEditorSite().getShell(), "GLSP Server Error",
               action.getMessage(), status);
         });
      }
   }

   protected GLSPDiagramEditorPart getEditorPart(final String clientId) {
      return GLSPServerException.getOrThrow(
         GLSPEditorIntegrationPlugin.getDefault().getGLSPEditorRegistry().getGLSPEditor(clientId),
         "Could not retrieve GLSP Editor. GLSP editor is not properly configured for clientId: " + clientId);
   }

   private static Optional<IStatus> toStatus(final ServerMessageAction action) {
      int severity = toSeverity(action.getSeverity());
      String message = action.getMessage();
      if (message == null || message.isEmpty()) {
         message = action.getDetails();
      } else if (action.getDetails() != null && !action.getDetails().isEmpty()) {
         message = message + "\n" + action.getDetails();
      }
      if (message != null && !message.isEmpty()) {
         return Optional.of(new Status(severity, EclipseEditorActionDispatcher.class, message));
      }
      return Optional.empty();
   }

   private static int toSeverity(final String glspSeverity) {
      switch (Severity.valueOf(glspSeverity)) {
         case NONE:
            return IStatus.OK;
         case OK:
            return IStatus.OK;
         case INFO:
            return IStatus.INFO;
         case WARNING:
            return IStatus.WARNING;
         case ERROR:
            return IStatus.ERROR;
         case FATAL:
            return IStatus.CANCEL;
         default:
            return IStatus.OK;
      }
   }

}

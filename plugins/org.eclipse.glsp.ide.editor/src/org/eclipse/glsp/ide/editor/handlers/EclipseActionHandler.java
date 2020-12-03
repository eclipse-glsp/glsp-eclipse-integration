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
package org.eclipse.glsp.ide.editor.handlers;

import static org.eclipse.glsp.server.utils.ServerMessageUtil.error;

import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramEditorPart;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.ActionMessage;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.model.ModelStateProvider;
import org.eclipse.glsp.server.protocol.GLSPClient;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * An abstract Handler to delegate the execution of Eclipse Commands to GLSP Actions,
 * via the ActionDispatcher of the current GLSP diagram.
 */
public abstract class EclipseActionHandler extends AbstractHandler {

   private final Logger log = Logger.getLogger(getClass());

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {
      IWorkbenchPartSite partSite = HandlerUtil.getActivePart(event).getSite();
      IEclipseContext context = partSite.getService(IEclipseContext.class);
      execute(context);
      return null;
   }

   protected abstract void execute(IEclipseContext context);

   protected void dispatchMessage(final IEclipseContext context, final Action action) {
      ActionDispatcher dispatcher = context.get(ActionDispatcher.class);
      String clientId = (String) context.get(GLSPDiagramEditorPart.GLSP_CLIENT_ID);
      // Note: GLSPClient is not available at the moment, as we don't have a way to track the
      // client connection lifecycle in the Eclipse Integration yet.
      Optional<GLSPClient> client = Optional.ofNullable(context.get(GLSPClient.class));
      dispatcher.dispatch(new ActionMessage(clientId, action))
         .exceptionally(ex -> handleError(ex, client, clientId, action));
   }

   protected <T> Optional<T> getInstance(final IEclipseContext context, final Class<T> type) {
      IEditorPart editor = context.get(IEditorPart.class);
      return editor instanceof GLSPDiagramEditorPart
         ? Optional.ofNullable(((GLSPDiagramEditorPart) editor).getInjector().getInstance(type))
         : Optional.empty();
   }

   protected Optional<GModelState> getModelState(final IEclipseContext context) {
      String clientId = (String) context.get(GLSPDiagramEditorPart.GLSP_CLIENT_ID);
      return getInstance(context, ModelStateProvider.class)
         .flatMap(stateProvider -> stateProvider.getModelState(clientId));
   }

   protected Optional<IEclipseContext> findContext() {
      IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
      if (activeEditor instanceof GLSPDiagramEditorPart) {
         return Optional.ofNullable(activeEditor.getSite().getService(IEclipseContext.class));
      }
      return Optional.empty();
   }

   protected Void handleError(final Throwable ex, final Optional<GLSPClient> client, final String clientId,
      final Action action) {
      String errorMsg = "Could not process action:" + action;
      log.error("[ERROR] " + errorMsg, ex);
      client.ifPresent(c -> c.process(new ActionMessage(clientId, error("[GLSP-Server] " + errorMsg, ex))));
      return null;
   }

}

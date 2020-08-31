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
package org.eclipse.glsp.integration.editor;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.glsp.api.action.ActionDispatcher;
import org.eclipse.glsp.api.action.kind.ExportSVGAction;
import org.eclipse.glsp.api.protocol.GLSPServerException;
import org.eclipse.glsp.integration.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.integration.editor.utils.UIUtil;

import com.google.inject.Injector;

public class ExportDiagramHandler extends AbstractHandler {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {

      UIUtil.getActiveEditor(GLSPDiagramEditorPart.class).ifPresent(editorPart -> {
         String clientId = (editorPart).getClientId();
         ActionDispatcher actionDispatcher = getInjector(editorPart).getInstance(ActionDispatcher.class);

         actionDispatcher.dispatch(clientId, new ExportSVGAction());

      });
      return null;
   }

   protected Injector getInjector(final GLSPDiagramEditorPart editorPart) {
      Optional<Injector> injector = GLSPEditorIntegrationPlugin.getDefault().getServerProviderRegistry()
         .getInjector(editorPart);
      if (!injector.isPresent()) {
         throw new GLSPServerException(
            "Could not retrieve GLSP injector. GLSP editor is not properly configured: "
               + editorPart.getEditorSite().getId());
      }
      return injector.get();
   }

}

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
package org.eclipse.glsp.ide.editor.ui;

import java.util.Optional;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.GLSPDiagramEditorPart;
import org.eclipse.glsp.ide.editor.actions.GLSPActionProvider;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.model.ModelStateProvider;
import org.eclipse.glsp.server.types.EditorContext;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.menus.IWorkbenchContribution;
import org.eclipse.ui.services.IServiceLocator;

/**
 * A dynamic {@link ContributionItem} that delegates to a {@link GLSPActionProvider} to populate
 * a {@link Menu}.
 */
public class GLSPDynamicContribution extends ContributionItem implements IWorkbenchContribution {

   private IServiceLocator serviceLocator;

   @Override
   public void initialize(final IServiceLocator serviceLocator) {
      this.serviceLocator = serviceLocator;
   }

   @Override
   public boolean isDynamic() { return true; }

   @Override
   public void fill(final Menu menu, final int index) {
      super.fill(menu, index);
      IEclipseContext context = serviceLocator.getService(IEclipseContext.class);
      GLSPActionProvider actionProvider = context.get(GLSPActionProvider.class);
      if (actionProvider != null) {
         GLSPDiagramEditorPart editor = context.get(GLSPDiagramEditorPart.class);
         String clientId = (String) context.get(GLSPDiagramEditorPart.GLSP_CLIENT_ID);
         // The model state will not be stored in the EclipseContext, as we (currently) have no way
         // to hook into new client connections. The Editor UI will be created and ready before the Browser
         // connects to the Backend server; so we may not have a ModelState yet.
         Optional<GModelState> modelState = editor.getInjector().getInstance(ModelStateProvider.class)
            .getModelState(clientId);
         if (modelState.isPresent()) {
            EditorContext editorContext = serviceLocator.getService(EditorContext.class);
            actionProvider.fillContextMenu(menu, modelState.get(), editorContext, index);
         } // The context menu may be initialized/requested before the editor is fully started. Simply ignore this.
      }
   }

}

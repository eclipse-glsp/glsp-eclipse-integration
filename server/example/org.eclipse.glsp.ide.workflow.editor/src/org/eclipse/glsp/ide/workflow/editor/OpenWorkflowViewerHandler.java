/********************************************************************************
 * Copyright (c) 2023 EclipseSource and others.
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

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MArea;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.ISelectionService;

/**
 * This is an example to open the E4 Part on a file basis.
 * If you want to work with files it is probably better to use the E3 Editor.
 *
 * If you have a use case where you are not working on a file, try the E4 Solution
 */
public class OpenWorkflowViewerHandler {

   @Execute
   public void execute(final EModelService modelService, final MApplication app, final EPartService partService,
      final ISelectionService selectionService) {
      MPart viwerPart = (MPart) modelService.cloneSnippet(app,
         "org.eclipse.glsp.ide.workflow.editor.part.workflowViewer",
         app.getSelectedElement());

      MUIElement editorssArea = modelService.find("org.eclipse.ui.editorss", app);

      MUIElement element = ((MPlaceholder) editorssArea).getRef();

      ((MPartStack) ((MArea) element).getChildren().get(0)).getChildren().add(viwerPart);

      ISelection selection = selectionService.getSelection();
      if (selection instanceof IStructuredSelection) {
         Object firstElement = ((IStructuredSelection) selection).getFirstElement();
         if (firstElement instanceof IFile) {
            IFile file = (IFile) firstElement;

            viwerPart.getProperties().put("file", Optional.ofNullable(file)
               .map(IFile::getLocationURI)
               .map(URI::getPath)
               .orElse(""));
         }
      }
      partService.activate(viwerPart);

   }
}

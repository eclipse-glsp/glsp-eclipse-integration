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
package org.eclipse.glsp.integration.editor.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenDiagramHandler extends AbstractHandler {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {
      ISelection selection = HandlerUtil.getCurrentSelection(event);
      // if (selection)
      return null;
   }

   // protected void getFile(ISelection selection) {
   // if(selection instanceof ITextSelection) {
   // val editorInput = PlatformUI.workbench.activeWorkbenchWindow?.activePage?.activeEditor?.editorInput
   // if(editorInput instanceof IFileEditorInput)
   // return editorInput.file
   // }
   // if (selection instanceof IStructuredSelection)
   // selection.toList.filter(IFile).head
   //
   // }
   //
}

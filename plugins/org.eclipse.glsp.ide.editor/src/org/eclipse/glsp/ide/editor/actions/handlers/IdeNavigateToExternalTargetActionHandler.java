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
package org.eclipse.glsp.ide.editor.actions.handlers;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.BasicActionHandler;
import org.eclipse.glsp.server.features.navigation.JsonOpenerOptions;
import org.eclipse.glsp.server.features.navigation.JsonOpenerOptions.TextSelection;
import org.eclipse.glsp.server.features.navigation.NavigateToExternalTargetAction;
import org.eclipse.glsp.server.features.navigation.NavigationTarget;
import org.eclipse.glsp.server.features.navigation.NavigationTargetProvider;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class IdeNavigateToExternalTargetActionHandler extends BasicActionHandler<NavigateToExternalTargetAction> {
   private static final Logger LOGGER = Logger.getLogger(IdeNavigateToExternalTargetActionHandler.class);

   @Override
   protected List<Action> executeAction(final NavigateToExternalTargetAction action, final GModelState modelState) {
      NavigationTarget target = action.getTarget();
      if (!target.getArgs().containsKey(NavigationTargetProvider.JSON_OPENER_OPTIONS)) {
         return none();
      }

      String jsonOpenerOptions = target.getArgs().get(NavigationTargetProvider.JSON_OPENER_OPTIONS);
      Optional<JsonOpenerOptions> options = JsonOpenerOptions.fromJson(jsonOpenerOptions);
      if (options.isEmpty()) {
         LOGGER.warn("Invalid format of JSON opener options: " + jsonOpenerOptions);
         return none();
      }

      URI targetURI = URI.create(target.getUri());
      Path location = new Path(targetURI.getPath());
      IFile workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
      if (workspaceFile != null && workspaceFile.exists()) {
         PlatformUI.getWorkbench().getDisplay().asyncExec(() -> openFile(workspaceFile, options.get()));
      } else {
         LOGGER.warn("Could not find workspace file for " + location);
      }
      return none();
   }

   protected void openFile(final IFile file, final JsonOpenerOptions options) {
      IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      if (activeWorkbenchWindow == null || activeWorkbenchWindow.getActivePage() == null) {
         LOGGER.warn("Could not find activate page to open file " + file);
         return;
      }
      try {
         IEditorPart editor = IDE.openEditor(activeWorkbenchWindow.getActivePage(), file);
         if (editor instanceof ITextEditor) {
            ITextEditor textEditor = (ITextEditor) editor;
            IRegion selection = translateSelection(textEditor, options.getSelection());
            textEditor.selectAndReveal(selection.getOffset(), selection.getLength());
         }
      } catch (PartInitException | BadLocationException exception) {
         LOGGER.error("Could not open file " + file + " with options " + options, exception);
      }
   }

   protected IRegion translateSelection(final ITextEditor editor, final TextSelection textSelection)
      throws BadLocationException {
      IDocument document = editor.getDocumentProvider().getDocument(editor.getEditorInput());
      IRegion startLine = document.getLineInformation(textSelection.getStart().getLine());
      IRegion endLine = document.getLineInformation(textSelection.getEnd().getLine());
      int startOffset = startLine.getOffset() + textSelection.getStart().getCharacter();
      int endOffset = endLine.getOffset() + textSelection.getEnd().getCharacter();
      int length = endOffset - startOffset;
      return new Region(startOffset, length);
   }
}

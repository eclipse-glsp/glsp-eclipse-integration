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
package org.eclipse.glsp.ide.editor.utils;

import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public final class UIUtil {
   private static final Logger LOGGER = LogManager.getLogger(UIUtil.class);

   private UIUtil() {}

   public static Optional<IWorkbench> getWorkbench() {
      if (PlatformUI.isWorkbenchRunning()) {
         return Optional.ofNullable(PlatformUI.getWorkbench());
      }
      return Optional.empty();
   }

   public static Optional<IWorkbenchWindow> getActiveWorkbenchWindow() {
      return getWorkbench().map(IWorkbench::getActiveWorkbenchWindow);
   }

   public static Optional<IWorkbenchPage> getActivePage() {
      return getActiveWorkbenchWindow().map(IWorkbenchWindow::getActivePage);
   }

   public static Optional<Display> findDisplay() {
      Display currentDisplay = Display.getCurrent();
      if (currentDisplay != null) {
         return Optional.of(currentDisplay);
      }
      Optional<Display> workbenchDisplay = getWorkbench().map(IWorkbench::getDisplay);
      return workbenchDisplay.isPresent()
         ? workbenchDisplay
         : Optional.ofNullable(Display.getDefault());
   }

   public static void asyncExec(final Runnable runnable) {
      Optional<Display> display = findDisplay();
      if (display.isPresent()) {
         display.get().asyncExec(runnable);
      } else {
         LOGGER.warn("Could not detect display, try running on current thread.");
         runnable.run();
      }
   }

   public static Optional<Shell> findShell(final String clientId) {
      return GLSPIdeEditorPlugin.getDefault().getGLSPEditorRegistry().getGLSPEditor(clientId)
         .map(composite -> composite.getShell())
         .or(UIUtil::findShell);
   }

   private static Optional<Shell> findShell() {
      return findDisplay().flatMap(display -> Optional.ofNullable(display.getActiveShell()));
   }

   public static Optional<IFile> getFirstSelectedFile(final ISelectionService selectionService) {
      ISelection selection = selectionService.getSelection();
      if (selection instanceof IStructuredSelection) {
         Object firstElement = ((IStructuredSelection) selection).getFirstElement();
         if (firstElement instanceof IFile) {
            return Optional.of((IFile) firstElement);
         }
      }
      return Optional.empty();
   }
}

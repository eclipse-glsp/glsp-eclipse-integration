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
package org.eclipse.glsp.integration.editor.utils;

import java.util.Optional;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class UIUtil {

   public static Optional<IWorkbenchWindow> getActiveWorkbenchWindow() {
      IWorkbench wb = PlatformUI.getWorkbench();
      if (wb != null) {
         return Optional.ofNullable(wb.getActiveWorkbenchWindow());
      }
      return Optional.empty();
   }

   public static Optional<IWorkbenchPage> getActivePage() {
      Optional<IWorkbenchWindow> workbenchWindow = getActiveWorkbenchWindow();
      if (workbenchWindow.isPresent()) {
         return Optional.ofNullable(workbenchWindow.get().getActivePage());
      }
      return Optional.empty();
   }

   public static Optional<IEditorPart> getActiveEditor() {
      Optional<IWorkbenchPage> activePage = getActivePage();
      if (activePage.isPresent()) {
         return Optional.ofNullable(activePage.get().getActiveEditor());
      }
      return Optional.empty();
   }

   public static <T extends IEditorPart> Optional<T> getActiveEditor(final Class<T> clazz) {
      return getActiveEditor().filter(clazz::isInstance).map(clazz::cast);
   }

}

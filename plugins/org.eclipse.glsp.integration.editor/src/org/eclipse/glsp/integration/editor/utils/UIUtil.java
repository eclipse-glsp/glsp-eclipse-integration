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

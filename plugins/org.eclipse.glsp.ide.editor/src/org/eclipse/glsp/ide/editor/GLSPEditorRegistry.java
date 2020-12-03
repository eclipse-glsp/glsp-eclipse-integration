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
package org.eclipse.glsp.ide.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramEditorPart;
import org.eclipse.glsp.ide.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;

public class GLSPEditorRegistry {
   private static String EDITOR_INTEGRATION_EXTENSION_POINT = "org.eclipse.glsp.editor.integration";
   private static String SERVER_MANAGER_CLASS_ATTRIBUTE = "serverManagerClass";
   private static String GLSP_EDITOR_ID_ATTRIBUTE = "editorId";
   private final Map<String, GLSPServerManager> editorIdToServerManager;
   private final Map<String, GLSPDiagramEditorPart> clientIdtoDiagramEditorPart;

   public GLSPEditorRegistry() {
      editorIdToServerManager = new HashMap<>();
      clientIdtoDiagramEditorPart = new HashMap<>();

      UIUtil.getActiveWorkbenchWindow()
         .ifPresent(window -> window.getPartService().addPartListener(new GLSPDiagramEditorPartListener()));
      obtainProvidersFromRegistry();
   }

   @SuppressWarnings("checkstyle:illegalCatch")
   protected void obtainProvidersFromRegistry() {
      IConfigurationElement[] config = Platform.getExtensionRegistry()
         .getConfigurationElementsFor(EDITOR_INTEGRATION_EXTENSION_POINT);

      for (IConfigurationElement element : config) {
         try {
            Object classObject = element.createExecutableExtension(SERVER_MANAGER_CLASS_ATTRIBUTE);
            String editorId = element.getAttribute(GLSP_EDITOR_ID_ATTRIBUTE);
            if (classObject instanceof GLSPServerManager) {
               GLSPServerManager serverManager = (GLSPServerManager) classObject;
               serverManager.start();
               editorIdToServerManager.put(editorId, serverManager);
            }

         } catch (Exception e) {
            GLSPEditorIntegrationPlugin.error("Exception while obtaining registered converters", e);
         }
      }

   }

   public Optional<GLSPServerManager> getGLSPServerManager(final GLSPDiagramEditorPart diagramEditorPart) {
      return getGLSPServerManager(diagramEditorPart.getEditorId());
   }

   public Optional<GLSPServerManager> getGLSPServerManager(final String editorId) {
      return Optional.of(editorIdToServerManager.get(editorId));
   }

   public Optional<GLSPDiagramEditorPart> getGLSPEditor(final String clientId) {
      return Optional.ofNullable(clientIdtoDiagramEditorPart.get(clientId));
   }

   class GLSPDiagramEditorPartListener implements IPartListener {

      @Override
      public void partActivated(final IWorkbenchPart part) {

      }

      @Override
      public void partBroughtToTop(final IWorkbenchPart part) {}

      @Override
      public void partClosed(final IWorkbenchPart part) {
         if (part instanceof GLSPDiagramEditorPart)

         {
            GLSPDiagramEditorPart editorPart = (GLSPDiagramEditorPart) part;
            clientIdtoDiagramEditorPart.remove(editorPart.getClientId());
         }
      }

      @Override
      public void partDeactivated(final IWorkbenchPart part) {

      }

      @Override
      public void partOpened(final IWorkbenchPart part) {
         if (part instanceof GLSPDiagramEditorPart) {
            GLSPDiagramEditorPart editorPart = (GLSPDiagramEditorPart) part;
            clientIdtoDiagramEditorPart.put(editorPart.getClientId(), editorPart);
         }
      }

   }
}

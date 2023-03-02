/********************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
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
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramEditor;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.glsp.server.types.GLSPServerException;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public class GLSPEditorRegistry {
   private static String EDITOR_INTEGRATION_EXTENSION_POINT = "org.eclipse.glsp.ide.editor";
   private static String SERVER_MANAGER_CLASS_ATTRIBUTE = "serverManagerClass";
   private static String GLSP_EDITOR_ID_ATTRIBUTE = "editorId";
   private final Map<String, GLSPServerManager> editorIdToServerManager;
   private final Map<String, GLSPDiagramEditor> clientIdtoDiagramEditor;

   public GLSPEditorRegistry() {
      editorIdToServerManager = new HashMap<>();
      clientIdtoDiagramEditor = new HashMap<>();

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

         } catch (Exception exception) {
            GLSPIdeEditorPlugin.error("Exception while obtaining registered converters", exception);
         }
      }
   }

   public Optional<GLSPServerManager> getGLSPServerManager(final GLSPDiagramEditor diagramEditor) {
      return getGLSPServerManager(diagramEditor.getEditorId());
   }

   public Optional<GLSPServerManager> getGLSPServerManager(final String editorId) {
      return Optional.of(editorIdToServerManager.get(editorId));
   }

   public synchronized Optional<GLSPDiagramEditor> getGLSPEditor(final String clientId) {
      return Optional.ofNullable(clientIdtoDiagramEditor.get(clientId));
   }

   public GLSPDiagramEditor getGLSPEditorOrThrow(final String clientId) {
      return GLSPServerException.getOrThrow(getGLSPEditor(clientId),
         "Could not retrieve GLSP Editor. GLSP editor is not properly configured for clientId: " + clientId);
   }

   private void partClosed(final IWorkbenchPartReference part) {
      if (part.getPart(false) instanceof GLSPDiagramEditor) {
         GLSPDiagramEditor editor = (GLSPDiagramEditor) part.getPart(false);
         editor.notifyAboutToBeDisposed();
         removeDiagramEditor(editor.getClientId());
      }
   }

   private synchronized void partOpened(final IWorkbenchPartReference part) {
      if (part.getPart(false) instanceof GLSPDiagramEditor) {
         GLSPDiagramEditor editor = (GLSPDiagramEditor) part.getPart(false);
         clientIdtoDiagramEditor.put(editor.getClientId(), editor);
      }
   }
   
   private synchronized void removeDiagramEditor(final String clientID) {
      clientIdtoDiagramEditor.remove(clientID);
   }

   class GLSPDiagramEditorPartListener implements IPartListener2 {
      @Override
      public void partClosed(final IWorkbenchPartReference part) {
         GLSPEditorRegistry.this.partClosed(part);
      }

      @Override
      public void partOpened(final IWorkbenchPartReference part) {
         GLSPEditorRegistry.this.partOpened(part);
      }
   }
}

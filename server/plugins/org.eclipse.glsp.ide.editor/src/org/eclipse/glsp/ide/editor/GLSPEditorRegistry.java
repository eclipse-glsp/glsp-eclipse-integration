/********************************************************************************
 * Copyright (c) 2020-2024 EclipseSource and others.
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
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramComposite;
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramEditor;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.glsp.server.types.GLSPServerException;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPartReference;

public class GLSPEditorRegistry {
   protected static String EDITOR_INTEGRATION_EXTENSION_POINT = "org.eclipse.glsp.ide.editor";
   protected static String SERVER_MANAGER_CLASS_ATTRIBUTE = "serverManagerClass";
   protected static String GLSP_EDITOR_ID_ATTRIBUTE = "editorId";
   protected final Map<String, GLSPServerManager> editorIdToServerManager;
   protected final Map<String, GLSPDiagramComposite> clientIdtoDiagramComposite;

   public GLSPEditorRegistry() {
      editorIdToServerManager = new HashMap<>();
      clientIdtoDiagramComposite = new HashMap<>();

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

   public Optional<GLSPServerManager> getGLSPServerManager(final GLSPDiagramComposite diagramComposite) {
      return getGLSPServerManager(diagramComposite.getEditorId());
   }

   public Optional<GLSPServerManager> getGLSPServerManager(final String editorId) {
      return Optional.of(editorIdToServerManager.get(editorId));
   }

   public synchronized Optional<GLSPDiagramComposite> getGLSPEditor(final String clientId) {
      return Optional.ofNullable(clientIdtoDiagramComposite.get(clientId));
   }

   public GLSPDiagramComposite getGLSPEditorOrThrow(final String clientId) {
      return GLSPServerException.getOrThrow(getGLSPEditor(clientId),
         "Could not retrieve GLSP Editor. GLSP editor is not properly configured for clientId: " + clientId);
   }

   public void registerComposite(final GLSPDiagramComposite diagram) {
      clientIdtoDiagramComposite.put(diagram.getClientId(), diagram);
   }

   protected void partClosed(final IWorkbenchPartReference part) {
      if (part.getPart(false) instanceof GLSPDiagramEditor) {
         GLSPDiagramEditor editor = (GLSPDiagramEditor) part.getPart(false);
         editor.notifyAboutToBeDisposed();
         removeDiagramEditor(editor.getClientId());
      }
   }

   protected synchronized void partOpened(final IWorkbenchPartReference part) {
      if (part.getPart(false) instanceof GLSPDiagramEditor) {
         GLSPDiagramEditor editor = (GLSPDiagramEditor) part.getPart(false);
         clientIdtoDiagramComposite.put(editor.getClientId(), editor.getDiagram());
      }
   }

   protected synchronized void removeDiagramEditor(final String clientID) {
      clientIdtoDiagramComposite.remove(clientID);
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

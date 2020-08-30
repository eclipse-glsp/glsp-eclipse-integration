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
package org.eclipse.glsp.integration.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.glsp.integration.editor.ui.GLSPEditorIntegrationPlugin;

import com.google.inject.Injector;

public class GLSPServerManagerRegistry {
   private static String EDITOR_INTEGRATION_EXTENSION_POINT = "org.eclipse.glsp.editor.integration";
   private static String SERVER_MANAGER_CLASS_ATTRIBUTE = "serverManagerClass";
   private static String GLSP_EDITOR_ID_ATTRIBUTE = "editorId";
   private final Map<String, GLSPServerManager> serverManagers;

   public GLSPServerManagerRegistry() {
      serverManagers = new HashMap<>();
      obtainProvidersFromRegistry();
   }

   private void obtainProvidersFromRegistry() {
      IConfigurationElement[] config = Platform.getExtensionRegistry()
         .getConfigurationElementsFor(EDITOR_INTEGRATION_EXTENSION_POINT);

      for (IConfigurationElement element : config) {
         try {
            Object classObject = element.createExecutableExtension(SERVER_MANAGER_CLASS_ATTRIBUTE);
            String editorId = element.getAttribute(GLSP_EDITOR_ID_ATTRIBUTE);
            if (classObject instanceof GLSPServerManager) {
               GLSPServerManager serverManager = (GLSPServerManager) classObject;
               serverManager.start();
               serverManagers.put(editorId, serverManager);
            }
         } catch (Exception e) {
            GLSPEditorIntegrationPlugin.error("Exception while obtaining registered converters", e);
         }
      }

   }

   public Optional<GLSPServerManager> getGLSPServerManager(final GLSPDiagramEditorPart diagramEditorPart) {
      return getGLSPServerManager(diagramEditorPart.getEditorSite().getId());
   }

   public Optional<GLSPServerManager> getGLSPServerManager(final String editorId) {
      return Optional.of(serverManagers.get(editorId));
   }

   public Optional<Injector> getInjector(final GLSPDiagramEditorPart diagramEditorPart) {
      return getInjector(diagramEditorPart.getEditorSite().getId());
   }

   public Optional<Injector> getInjector(final String editorId) {
      return getGLSPServerManager(editorId).map(GLSPServerManager::getInjector);
   }
}

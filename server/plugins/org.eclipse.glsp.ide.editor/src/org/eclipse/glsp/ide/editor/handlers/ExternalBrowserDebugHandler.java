/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
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
package org.eclipse.glsp.ide.editor.handlers;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.ui.GLSPDiagramEditor;
import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ExternalBrowserDebugHandler extends IdeActionHandler {
   private static Logger LOG = Logger.getLogger(ExternalBrowserDebugHandler.class);

   @Override
   protected void execute(final IEclipseContext context) {
      String clientId = (String) context.get(GLSPDiagramEditor.GLSP_CLIENT_ID);
      Optional<GLSPDiagramEditor> editor = GLSPIdeEditorPlugin.getDefault().getGLSPEditorRegistry()
         .getGLSPEditor(clientId);
      if (editor.isPresent()) {
         String url = editor.get().getBrowserUrl();
         openInExternalBrowser(url);
         return;
      }
      LOG.warn("Execute failed, could not retrieve active GLSPDiagramEditor");
   }

   protected void openInExternalBrowser(final String url) {
      try {
         PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL(url));
      } catch (PartInitException | MalformedURLException e) {
         LOG.error("Could not open url in external browser. Url: " + url, e);
      }
   }

}

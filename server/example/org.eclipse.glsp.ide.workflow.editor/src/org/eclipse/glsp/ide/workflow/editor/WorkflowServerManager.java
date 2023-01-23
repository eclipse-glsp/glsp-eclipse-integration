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
package org.eclipse.glsp.ide.workflow.editor;

import java.net.URL;

import org.eclipse.elk.alg.layered.options.LayeredMetaDataProvider;
import org.eclipse.glsp.ide.editor.GLSPServerManager;
import org.eclipse.glsp.ide.editor.di.IdeServerModule;
import org.eclipse.glsp.layout.ElkLayoutEngine;
import org.eclipse.glsp.server.di.ServerModule;

public class WorkflowServerManager extends GLSPServerManager {

   public WorkflowServerManager() {}

   @Override
   public ServerModule configureServerModule() {
      return new IdeServerModule().configureDiagramModule(new WorkflowGLSPEclipseModule());
   }

   @Override
   public URL getResourceURL() { return Activator.getDefault().getBundle().getResource("diagram"); }

   @Override
   protected void preConfigure() {
      ElkLayoutEngine.initialize(new LayeredMetaDataProvider());
   }

   @Override
   public String getGlspId() { return "workflow"; }
}

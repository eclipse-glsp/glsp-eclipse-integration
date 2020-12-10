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
package org.eclipse.glsp.ide.editor.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.ide.editor.GLSPEditorRegistry;
import org.osgi.framework.BundleContext;

public class GLSPIdeEditorPlugin extends Plugin {
   // The plug-in ID
   public static final String PLUGIN_ID = "org.eclipse.glsp.ide.editor"; //$NON-NLS-1$

   // The shared instance
   private static GLSPIdeEditorPlugin instance;
   private GLSPEditorRegistry glspEditorRegistry;

   @Override
   public void start(final BundleContext context) throws Exception {
      super.start(context);
      instance = this;
      glspEditorRegistry = new GLSPEditorRegistry();
   }

   @Override
   public void stop(final BundleContext context) throws Exception {
      instance = null;
      super.stop(context);
   }

   public GLSPEditorRegistry getGLSPEditorRegistry() { return glspEditorRegistry; }

   public static void error(final String msg, final Throwable e) {
      getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e));
   }

   /**
    * Returns the shared instance.
    *
    * @return the shared instance
    */
   public static GLSPIdeEditorPlugin getDefault() { return instance; }

   public static GLSPEditorRegistry getDefaultGLSPEditorRegistry() { return getDefault().getGLSPEditorRegistry(); }
}

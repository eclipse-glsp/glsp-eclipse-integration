package org.eclipse.glsp.integration.editor.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.integration.editor.GLSPServerManagerRegistry;
import org.osgi.framework.BundleContext;

public class GLSPEditorIntegrationPlugin extends Plugin {
   // The plug-in ID
   public static final String PLUGIN_ID = "org.eclipse.glsp.integration.editor"; //$NON-NLS-1$

   // The shared instance
   private static GLSPEditorIntegrationPlugin instance;
   private GLSPServerManagerRegistry serverProviderRegistry;

   /**
    * The constructor
    */
   public GLSPEditorIntegrationPlugin() {

   }

   @Override
   public void start(final BundleContext context) throws Exception {
      super.start(context);
      serverProviderRegistry = new GLSPServerManagerRegistry();
      instance = this;
   }

   @Override
   public void stop(final BundleContext context) throws Exception {
      instance = null;
      super.stop(context);
   }

   public static void error(final String msg, final Throwable e) {
      GLSPEditorIntegrationPlugin.getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, msg, e));

   }

   /**
    * Returns the shared instance
    *
    * @return the shared instance
    */
   public static GLSPEditorIntegrationPlugin getDefault() { return instance; }

   public GLSPServerManagerRegistry getServerProviderRegistry() { return serverProviderRegistry; }

}

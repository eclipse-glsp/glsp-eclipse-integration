package org.eclipse.glsp.integration.editor;

import static org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_INFO_TSK;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_WARN_TSK;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.glsp.api.action.kind.ServerStatusAction;
import org.eclipse.glsp.api.protocol.GLSPServerException;
import org.eclipse.glsp.integration.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.EditorPart;

import com.google.gson.JsonObject;

public class GLSPDiagramEditorPart extends EditorPart {

   private static Logger LOG = Logger.getLogger(GLSPDiagramEditorPart.class);

   private Browser browser;

   private String filePath;

   private Composite comp;
   private Composite statusBar;
   private Label statusBarIcon;
   private Label statusBarMessage;
   private final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();

   private boolean connected;

   public GLSPDiagramEditorPart() {}

   public GLSPServerManager getServerManager() {
      Optional<GLSPServerManager> serverManager = GLSPEditorIntegrationPlugin.getDefault().getServerProviderRegistry()
         .getGLSPServerManager(this);
      if (!serverManager.isPresent()) {
         throw new GLSPServerException(
            "Could not retrieve GLSPServerManager. GLSP editor is not properly configured: " + getEditorSite().getId());
      }
      return serverManager.get();
   }

   @Override
   public void doSave(final IProgressMonitor monitor) {
      // TODO Auto-generated method stub

   }

   @Override
   public void doSaveAs() {
      // TODO Auto-generated method stub

   }

   @Override
   public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
      if (!(input instanceof IFileEditorInput)) {
         throw new PartInitException("Invalid Input: Must be IFileEditorInput");
      }
      IFileEditorInput fileInput = (IFileEditorInput) input;
      filePath = fileInput.getFile().getLocationURI().getPath();

      setSite(site);
      setInput(input);

   }

   @Override
   public boolean isDirty() { return false; }

   @Override
   public boolean isSaveAsAllowed() {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void createPartControl(final Composite parent) {
      comp = new Composite(parent, SWT.NO_SCROLL);
      comp.setLayout(new GridLayout(1, true));

      browser = new Browser(comp, SWT.NO_SCROLL);
      browser.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
      Browser.clearSessions();
      browser.refresh();

      statusBar = new Composite(comp, SWT.NO_SCROLL);
      statusBar.setLayout(new RowLayout());
      GridData gridData = new GridData();
      gridData.exclude = true;
      statusBar.setLayoutData(gridData);

      statusBarIcon = new Label(statusBar, SWT.NONE);
      statusBarIcon.setImage(sharedImages.getImage(IMG_OBJS_INFO_TSK));

      statusBarMessage = new Label(statusBar, SWT.NONE);
      statusBarMessage.setText("");

      connect(filePath);
      setPartName(getFileName());
      browser.addMouseTrackListener(new MouseTrackAdapter() {

         @Override
         public void mouseEnter(final MouseEvent e) {
            String script = "var event = new MouseEvent('mouseup', {" + //
               "});" + //
               "document.getElementById(\"sprotty\").children[0].dispatchEvent(event);";
            browser.execute(script);
         }

      });

      browser.addControlListener(new ControlAdapter() {

         @Override
         public void controlResized(final ControlEvent e) {
            super.controlResized(e);
            if (connected) {
               Point size = ((Control) e.widget).getSize();
               // JsonObject newCanvasBoundsAction = actionGenerator
               // .initializeCanvasBoundsAction(GraphUtil.bounds(0, 0, size.x, size.y));
               // sendAction(newCanvasBoundsAction);
            }
         }

      });
      browser.refresh();
   }

   @Override
   public void dispose() {
      disconnect();
      super.dispose();
   }

   public String getClientId() { return getClass().getSimpleName() + "_" + hashCode(); }

   protected String getFileName() { return FilenameUtils.getName(filePath); }

   public void showServerState(final ServerStatusAction serverStatus) {
      switch (serverStatus.getSeverity().toLowerCase()) {
         case "ok": {
            statusBar.setVisible(false);
            ((GridData) statusBar.getLayoutData()).exclude = true;
            break;
         }
         case "error": {
            statusBar.setVisible(true);
            ((GridData) statusBar.getLayoutData()).exclude = false;
            statusBarIcon.setImage(sharedImages.getImage(IMG_OBJS_ERROR_TSK));
            break;
         }
         case "warning": {
            statusBar.setVisible(true);
            ((GridData) statusBar.getLayoutData()).exclude = false;
            statusBarIcon.setImage(sharedImages.getImage(IMG_OBJS_WARN_TSK));
            break;
         }
         case "info": {
            statusBar.setVisible(true);
            ((GridData) statusBar.getLayoutData()).exclude = false;
            statusBarIcon.setImage(sharedImages.getImage(IMG_OBJS_INFO_TSK));
            break;
         }
      }
      statusBarMessage.setText(serverStatus.getMessage());
      comp.layout(true, true);
   }

   @Override
   public void setFocus() {
      browser.setFocus();
   }

   public void connect(final String path) {

      try {
         GLSPServerManager manager = getServerManager();
         ServerConnector connector = Stream.of(manager.getServer().getConnectors()).findFirst()
            .map(ServerConnector.class::cast).orElse(null);

         if (connector != null) {
            String url = String.format("http://%s:%s/diagram.html?client=%s&path=%s", connector.getHost(),
               connector.getPort(), encodeParameter(getClientId()), encodeParameter(path));
            browser.setUrl(
               url);
            System.out.println(url);
            this.connected = true;

         }

      } catch (Exception e) {
         e.printStackTrace();
      }

   }

   protected String encodeParameter(final String parameter) throws UnsupportedEncodingException {
      return URLEncoder.encode(parameter, "UTF-8");
   }

   public void disconnect() {
      // TODO Auto-generated method stub

   }

   public void sendAction(final JsonObject action) {
      // TODO Auto-generated method stub

   }

   public static URI toUri(final File file) {
      // URI scheme specified by language server protocol and LSP
      try {
         return new URI("file", "", file.getAbsoluteFile().toURI().getPath(), null); //$NON-NLS-1$ //$NON-NLS-2$
      } catch (URISyntaxException e) {
         return file.getAbsoluteFile().toURI();
      }
   }
}

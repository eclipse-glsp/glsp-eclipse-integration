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

import static org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_INFO_TSK;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_WARN_TSK;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.GLSPServerManager;
import org.eclipse.glsp.ide.editor.actions.GLSPActionProvider;
import org.eclipse.glsp.ide.editor.di.EclipseEditorActionDispatcher;
import org.eclipse.glsp.ide.editor.utils.GLSPDiagramEditorMarkerUtil;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.GLSPServerStatusAction;
import org.eclipse.glsp.server.actions.SaveModelAction;
import org.eclipse.glsp.server.actions.ServerStatusAction;
import org.eclipse.glsp.server.actions.SetDirtyStateAction;
import org.eclipse.glsp.server.features.contextactions.RequestContextActions;
import org.eclipse.glsp.server.features.navigation.NavigateToTargetAction;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.model.ModelStateProvider;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.types.EditorContext;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

import com.google.gson.JsonObject;
import com.google.inject.Injector;

public class GLSPDiagramEditorPart extends EditorPart implements IGotoMarker {
   /**
    * {@link IEclipseContext} key for the current client id. The associated value
    * is a {@link String}.
    */
   public static final String GLSP_CLIENT_ID = "GLSP_CLIENT_ID";
   private static final AtomicInteger COUNT = new AtomicInteger(0);

   private final Timer statusTimer = new Timer();

   private Browser browser;

   private String filePath;

   private Composite comp;
   private Composite statusBar;
   private Label statusBarIcon;
   private Label statusBarMessage;
   private final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
   private String clientId;
   private boolean dirty;

   private boolean connected;
   private final CompletableFuture<Injector> injector = new CompletableFuture<>();

   private ServerStatusAction currentStatus;

   public GLSPDiagramEditorPart() {}

   public GLSPServerManager getServerManager() {
      Optional<GLSPServerManager> serverManager = GLSPEditorIntegrationPlugin.getDefault().getGLSPEditorRegistry()
         .getGLSPServerManager(this);
      if (!serverManager.isPresent()) {
         throw new GLSPServerException(
            "Could not retrieve GLSPServerManager. GLSP editor is not properly configured: "
               + getEditorSite().getId());
      }
      return serverManager.get();
   }

   public Optional<GLSPActionProvider> getActionProvider() { return Optional.empty(); }

   public String getEditorId() { return getConfigurationElement().getAttribute("id"); }

   @Override
   public void doSave(final IProgressMonitor monitor) {
      dispatch(new SaveModelAction());
   }

   @Override
   public void doSaveAs() {

   }

   @Override
   public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
      if (!(input instanceof IFileEditorInput)) {
         throw new PartInitException("Invalid Input: Must be IFileEditorInput");
      }
      IFileEditorInput fileInput = (IFileEditorInput) input;
      filePath = fileInput.getFile().getLocationURI().getPath();
      this.clientId = getServerManager().getGlspId() + "_Editor_" + COUNT.incrementAndGet();
      setSite(site);
      setInput(input);

      IEclipseContext context = site.getService(IEclipseContext.class);
      configureContext(context);
   }

   /**
    * Publish some of the GLSP-specific services to the {@link IEclipseContext}.
    *
    * @param context
    */
   private void configureContext(final IEclipseContext context) {
      GLSPServerManager serverManager = getServerManager();
      context.set(GLSPServerManager.class, serverManager);
      context.set(GLSP_CLIENT_ID, clientId);
      getActionProvider().ifPresent(provider -> context.set(GLSPActionProvider.class, provider));
      // Editor context contains some info about the current client state. It can
      // be updated when the client sends new actions
      context.declareModifiable(EditorContext.class);
   }

   @Override
   public boolean isDirty() { return dirty; }

   @Override
   public boolean isSaveAsAllowed() { return false; }

   public void handle(final SetDirtyStateAction action) {
      if (this.dirty != action.isDirty()) {
         this.dirty = action.isDirty();
         firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY);
      }
   }

   @Override
   public void createPartControl(final Composite parent) {
      comp = new Composite(parent, SWT.NO_SCROLL);
      comp.setLayout(new GridLayout(1, true));

      browser = createBrowser(comp);
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
               // Point size = ((Control) e.widget).getSize();
               // JsonObject newCanvasBoundsAction = actionGenerator
               // .initializeCanvasBoundsAction(GraphUtil.bounds(0, 0, size.x, size.y));
               // sendAction(newCanvasBoundsAction);
            }
         }

      });
      browser.refresh();

      MenuManager menuManager = new MenuManager();
      Menu menu = menuManager.createContextMenu(browser);
      getSite().registerContextMenu(menuManager, getSite().getSelectionProvider());
      browser.setMenu(menu);
   }

   protected Browser createBrowser(final Composite parent) {
      return new Browser(comp, SWT.NO_SCROLL);
   }

   @Override
   public void dispose() {
      disconnect();
      super.dispose();
   }

   public String getClientId() { return clientId; }

   protected String getFileName() { return FilenameUtils.getName(filePath); }

   public synchronized void showServerState(final ServerStatusAction serverStatus) {
      this.currentStatus = serverStatus;
      if (serverStatus instanceof GLSPServerStatusAction
         && ((GLSPServerStatusAction) serverStatus).getTimeout() > 0) {
         int timeout = ((GLSPServerStatusAction) serverStatus).getTimeout();
         statusTimer.schedule(new TimerTask() {

            @Override
            public void run() {
               if (currentStatus != serverStatus) {
                  /* different status is showing in the meantime */
                  return;
               }
               updateServerStatusUI("ok", "");

            }
         }, timeout);
      }
      updateServerStatusUI(serverStatus.getSeverity(), serverStatus.getMessage());
   }

   private synchronized void updateServerStatusUI(final String severity, final String message) {
      Display.getDefault().asyncExec(() -> {
         switch (severity.toLowerCase()) {
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
            default: {}
         }
         statusBarMessage.setText(message);
         comp.layout(true, true);
      });
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
            String url = String.format("http://%s:%s/diagram.html?client=%s&path=%s&port=%s", connector.getHost(),
               manager.getLocalPort(), encodeParameter(getClientId()), encodeParameter(path),
               manager.getLocalPort());
            browser.setUrl(url);
            System.out.println(url);
            this.connected = true;

         }

      } catch (UnsupportedEncodingException e) {
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

   public void showContextMenu(final RequestContextActions action) {
      if (!"context-menu".equals(action.getContextId())) {
         return;
      }
      // Update the EditorContext, as this is specific to each action.
      getSite().getService(IEclipseContext.class).set(EditorContext.class, action.getEditorContext());
      // Nothing more to do here; populating & opening the menu will be handled
      // directly
      // by the browser control.
   }

   /**
    * This method will be invoked once the client session is established, which
    * will happen asynchronously.
    *
    * @param injector
    */
   public void setInjector(final Injector injector) {
      this.injector.complete(injector);
      IEclipseContext context = getSite().getService(IEclipseContext.class);
      context.set(ActionDispatcher.class, injector.getInstance(ActionDispatcher.class));
   }

   /**
    * Return the GLSP Injector associated to this editor.
    *
    * @return
    *         The GLSP Injector associated to this editor.
    */
   public Injector getInjector() { return injector.getNow(null); }

   protected <T> CompletableFuture<T> getInstance(final Class<T> type) {
      return this.injector.thenApply(injector -> injector.getInstance(type));
   }

   protected CompletableFuture<ActionDispatcher> getActionDispatcher() { return getInstance(ActionDispatcher.class); }

   protected CompletableFuture<Optional<GModelState>> getModelState() {
      return getInstance(ModelStateProvider.class)
         .thenApply(modelStateProvider -> modelStateProvider.getModelState(this.clientId));
   }

   protected CompletableFuture<Void> dispatch(final Action action) {
      return getActionDispatcher().thenCompose(actionDispatcher -> actionDispatcher.dispatch(clientId, action));
   }

   protected CompletableFuture<Void> onceModelInitialized() {
      return getActionDispatcher().thenCompose(this::onceModelInitialized);
   }

   protected CompletableFuture<Void> onceModelInitialized(final ActionDispatcher actionDispatcher) {
      if (actionDispatcher instanceof EclipseEditorActionDispatcher) {
         return ((EclipseEditorActionDispatcher) actionDispatcher).onceModelInitialized();
      }
      return CompletableFuture.completedFuture(null);
   }

   @Override
   public void gotoMarker(final IMarker marker) {
      onceModelInitialized().thenCompose(initialized -> getModelState().thenAccept(modelState -> {
         GLSPDiagramEditorMarkerUtil.asNavigationTarget(marker, modelState)
            .map(NavigateToTargetAction::new)
            .ifPresent(this::dispatch);
      }));
   }
}

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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.GLSPServerManager;
import org.eclipse.glsp.ide.editor.actions.GLSPActionProvider;
import org.eclipse.glsp.ide.editor.actions.InvokeCopyAction;
import org.eclipse.glsp.ide.editor.actions.InvokeCutAction;
import org.eclipse.glsp.ide.editor.actions.InvokeDeleteAction;
import org.eclipse.glsp.ide.editor.actions.InvokePasteAction;
import org.eclipse.glsp.ide.editor.di.EclipseEditorActionDispatcher;
import org.eclipse.glsp.ide.editor.utils.GLSPDiagramEditorMarkerUtil;
import org.eclipse.glsp.ide.editor.utils.IdeClientOptions;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.actions.SaveModelAction;
import org.eclipse.glsp.server.actions.SelectAllAction;
import org.eclipse.glsp.server.actions.ServerStatusAction;
import org.eclipse.glsp.server.disposable.DisposableCollection;
import org.eclipse.glsp.server.features.contextactions.RequestContextActions;
import org.eclipse.glsp.server.features.navigation.NavigateToTargetAction;
import org.eclipse.glsp.server.features.undoredo.RedoAction;
import org.eclipse.glsp.server.features.undoredo.UndoAction;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.model.ModelStateProvider;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.types.EditorContext;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.ProgressListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

import com.google.inject.Injector;

public class GLSPDiagramEditor extends EditorPart implements IGotoMarker {
   /**
    * {@link IEclipseContext} key for the current client id. The associated value
    * is a {@link String}.
    */
   public static final String GLSP_CLIENT_ID = "GLSP_CLIENT_ID";

   protected static final Logger LOGGER = Logger.getLogger(GLSPDiagramEditor.class);
   protected static final String GLSP_CONTEXT_MENU_ID = "context-menu";
   protected static final AtomicInteger COUNT = new AtomicInteger(0);

   protected Composite root;
   protected Browser browser;
   protected GSLPDiagramEditorStatusBar statusBar;

   protected String widgetId;
   protected String clientId;

   protected final CompletableFuture<Injector> injector = new CompletableFuture<>();
   protected boolean dirty;

   protected final DisposableCollection toDispose = new DisposableCollection();

   protected final Map<String, IAction> globalActions = new HashMap<>(Map.of(
      ActionFactory.UNDO.getId(), actionFor(this::undo),
      ActionFactory.REDO.getId(), actionFor(this::redo),
      ActionFactory.CUT.getId(), actionFor(this::cut),
      ActionFactory.COPY.getId(), actionFor(this::copy),
      ActionFactory.PASTE.getId(), actionFor(this::paste),
      ActionFactory.DELETE.getId(), actionFor(this::delete),
      ActionFactory.SELECT_ALL.getId(), actionFor(this::selectAll)));

   public GLSPDiagramEditor() {}

   public GLSPServerManager getServerManager() {
      Optional<GLSPServerManager> serverManager = GLSPIdeEditorPlugin.getDefault().getGLSPEditorRegistry()
         .getGLSPServerManager(this);
      if (!serverManager.isPresent()) {
         throw new GLSPServerException(
            "Could not retrieve GLSPServerManager. GLSP editor is not properly configured: "
               + getEditorSite().getId());
      }
      return serverManager.get();
   }

   protected Optional<GLSPActionProvider> getActionProvider() { return Optional.empty(); }

   public String getEditorId() { return getConfigurationElement().getAttribute("id"); }

   public Map<String, IAction> getGlobalActions() { return globalActions; }

   public String getClientId() { return clientId; }

   protected void setClientId(final String clientId) { this.clientId = clientId; }

   public String getWidgetId() { return widgetId; }

   protected void setWidgetId(final String widgetId) { this.widgetId = widgetId; }

   protected String getFilePath() { return ((IFileEditorInput) getEditorInput()).getFile().getLocationURI().getPath(); }

   protected String getFileName() { return FilenameUtils.getName(getFilePath()); }

   @Override
   public void doSave(final IProgressMonitor monitor) {
      dispatch(new SaveModelAction());
   }

   @Override
   public boolean isSaveAsAllowed() { return false; }

   @Override
   public void doSaveAs() {}

   @Override
   public boolean isDirty() { return dirty; }

   public void setDirty(final boolean dirty) {
      if (this.dirty != dirty) {
         this.dirty = dirty;
         UIUtil.asyncExec(() -> firePropertyChange(IWorkbenchPartConstants.PROP_DIRTY));
      }
   }

   protected void undo() {
      dispatch(new UndoAction());
   }

   protected void redo() {
      dispatch(new RedoAction());
   }

   protected void copy() {
      dispatch(new InvokeCopyAction());
   }

   protected void cut() {
      dispatch(new InvokeCutAction());
   }

   protected void paste() {
      dispatch(new InvokePasteAction());
   }

   protected void delete() {
      dispatch(new InvokeDeleteAction());
   }

   protected void selectAll() {
      dispatch(new SelectAllAction(true));
   }

   public void showServerStatus(final ServerStatusAction action) {
      this.statusBar.showServerStatus(action);
   }

   public void handleRequestContext(final RequestContextActions action) {
      if (!GLSP_CONTEXT_MENU_ID.equals(action.getContextId())) {
         return;
      }
      // Update the EditorContext, as this is specific to each action.
      getSite().getService(IEclipseContext.class).set(EditorContext.class, action.getEditorContext());
      // Nothing more to do here; populating & opening the menu will be handled directly by the browser control.
   }

   @Override
   public void gotoMarker(final IMarker marker) {
      getModelStateOnceInitialized().thenAccept(modelState -> {
         GLSPDiagramEditorMarkerUtil.asNavigationTarget(marker, modelState)
            .map(NavigateToTargetAction::new)
            .ifPresent(this::dispatch);
      });
   }

   public void setInjector(final Injector injector) {
      this.injector.complete(injector);
      IEclipseContext context = getSite().getService(IEclipseContext.class);
      context.set(ActionDispatcher.class, injector.getInstance(ActionDispatcher.class));
   }

   public Injector getInjector() { return injector.getNow(null); }

   protected <T> CompletableFuture<T> getInstance(final Class<T> type) {
      return this.injector.thenApply(injector -> injector.getInstance(type));
   }

   protected CompletableFuture<ActionDispatcher> getActionDispatcher() { return getInstance(ActionDispatcher.class); }

   protected CompletableFuture<Optional<GModelState>> getModelState() {
      return getInstance(ModelStateProvider.class)
         .thenApply(modelStateProvider -> modelStateProvider.getModelState(getClientId()));
   }

   protected CompletableFuture<Optional<GModelState>> getModelStateOnceInitialized() {
      return onceModelInitialized().thenCompose(initialized -> getModelState());
   }

   protected CompletableFuture<Void> dispatch(final Action action) {
      return getActionDispatcher().thenCompose(actionDispatcher -> actionDispatcher.dispatch(getClientId(), action));
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
   public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
      if (!(input instanceof IFileEditorInput)) {
         throw new PartInitException("Invalid Input: Must be IFileEditorInput");
      }
      setSite(site);
      setInput(input);
      setClientId(getServerManager().getGlspId() + "_Editor_" + COUNT.incrementAndGet());
      setWidgetId(getClientId());

      IEclipseContext context = site.getService(IEclipseContext.class);
      configureContext(context);

      getModelStateOnceInitialized().thenAccept(this::syncMarkers);
   }

   @Override
   public void createPartControl(final Composite parent) {
      root = new Composite(parent, SWT.NO_SCROLL);
      root.setLayout(new GridLayout(1, true));

      setPartName(getFileName());

      this.browser = createBrowser(root);
      setupBrowser(this.browser, getFilePath());

      this.statusBar = createStatusBar(root);
   }

   /**
    * Publish some of the GLSP-specific services to the {@link IEclipseContext}.
    *
    * @param context
    */
   protected void configureContext(final IEclipseContext context) {
      GLSPServerManager serverManager = getServerManager();
      context.set(GLSPServerManager.class, serverManager);
      context.set(GLSP_CLIENT_ID, clientId);
      getActionProvider().ifPresent(provider -> context.set(GLSPActionProvider.class, provider));
      // Editor context contains some info about the current client state. It can
      // be updated when the client sends new actions
      context.declareModifiable(EditorContext.class);
   }

   protected void syncMarkers(final Optional<GModelState> modelState) {
      if (modelState.isPresent()) {
         IdeClientOptions.getSourceUriAsIFile(modelState.get().getClientOptions())
            .map(workspaceFile -> GLSPDiagramEditorMarkerUtil.syncMarkers(
               workspaceFile,
               modelState.get().getClientId(),
               getActionDispatcher().getNow(null)))
            .ifPresent(toDispose::add);
      }
   }

   protected Browser createBrowser(final Composite parent) {
      Browser browser = new FocusAwareBrowser(parent, SWT.NO_SCROLL | SWT.CHROMIUM);
      browser.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
      toDispose.add(browser::dispose);
      return browser;
   }

   protected void setupBrowser(final Browser browser, final String path) {
      Browser.clearSessions();
      browser.refresh();
      browser.addMouseTrackListener(MouseTrackListener.mouseEnterAdapter(this::mouseEnteredBrowser));
      browser.setMenu(createBrowserMenu());
      browser.addProgressListener(ProgressListener.completedAdapter(event -> installBrowserFunctions()));
      browser.setUrl(createBrowserUrl(path));
      browser.refresh();
   }

   protected void mouseEnteredBrowser(final MouseEvent event) {
      if (getWidgetId() != null) {
         // we dispatch a mouse up event to ensure the client has proper focus
         String dispatchMouseUp = "var element = document.getElementById(\"" + getWidgetId() + "\");"
            + "if(element) { "
            + "   const event = new MouseEvent('mouseup', {});"
            + "   element.children[0].dispatchEvent(event);"
            + "}";
         browser.execute(dispatchMouseUp);
      }
   }

   protected Menu createBrowserMenu() {
      MenuManager menuManager = new MenuManager();
      Menu menu = menuManager.createContextMenu(browser);
      getSite().registerContextMenu(menuManager, getSite().getSelectionProvider());
      return menu;
   }

   protected void installBrowserFunctions() {
      // browser functions are automatically disposed with the browser
      ChromiumKeyBindingFunction.install(GLSPDiagramEditor.this, browser);
      ChromiumSelectionFunction.install(GLSPDiagramEditor.this, browser);
   }

   protected String createBrowserUrl(final String path) {
      try {
         GLSPServerManager manager = getServerManager();
         ServerConnector connector = Stream.of(manager.getServer().getConnectors()).findFirst()
            .map(ServerConnector.class::cast)
            .orElse(null);

         if (connector != null) {
            String url = String.format("http://%s:%s/diagram.html?client=%s&path=%s&port=%s&widget=%s",
               connector.getHost(),
               manager.getLocalPort(),
               encodeParameter(getClientId()),
               encodeParameter(path),
               manager.getLocalPort(),
               encodeParameter(getWidgetId()));
            System.out.println(url);
            return url;
         }
      } catch (UnsupportedEncodingException exception) {
         LOGGER.error(exception);
      }
      return null;
   }

   protected String encodeParameter(final String parameter) throws UnsupportedEncodingException {
      return URLEncoder.encode(parameter, "UTF-8");
   }

   protected GSLPDiagramEditorStatusBar createStatusBar(final Composite parent) {
      GSLPDiagramEditorStatusBar statusBar = new GSLPDiagramEditorStatusBar(parent);
      statusBar.setLayout(new RowLayout());
      return statusBar;
   }

   @Override
   public void setFocus() {
      browser.setFocus();
   }

   @Override
   public void dispose() {
      super.dispose();
      this.toDispose.dispose();
   }

   protected static IAction actionFor(final Runnable runnable) {
      return new org.eclipse.jface.action.Action() {
         @Override
         public void run() {
            runnable.run();
         }
      };
   }
}

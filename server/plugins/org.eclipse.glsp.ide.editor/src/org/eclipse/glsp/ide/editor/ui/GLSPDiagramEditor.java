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
package org.eclipse.glsp.ide.editor.ui;

import java.net.URI;
import java.util.Optional;

import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.glsp.ide.editor.GLSPServerManager;
import org.eclipse.glsp.ide.editor.actions.GLSPActionProvider;
import org.eclipse.glsp.ide.editor.utils.GLSPDiagramEditorMarkerUtil;
import org.eclipse.glsp.ide.editor.utils.IdeClientOptions;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.glsp.server.actions.SaveModelAction;
import org.eclipse.glsp.server.actions.SelectAction;
import org.eclipse.glsp.server.disposable.DisposableCollection;
import org.eclipse.glsp.server.features.navigation.NavigateToTargetAction;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.types.GLSPServerException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPartConstants;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.EditorPart;

public class GLSPDiagramEditor extends EditorPart implements IGotoMarker, ISelectionProvider {

   protected boolean dirty;

   protected final SelectionManager selectionListener = new SelectionManager();

   protected final DisposableCollection toDispose = new DisposableCollection();

   protected GLSPDiagramComposite diagram;

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

   public String getClientId() { return diagram.getClientId(); }

   public GLSPDiagramComposite getDiagram() { return diagram; }

   @Override
   public void doSave(final IProgressMonitor monitor) {
      diagram.dispatch(new SaveModelAction());
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

   @Override
   public void gotoMarker(final IMarker marker) {
      diagram.getModelStateOnceInitialized().thenAccept(modelState -> {
         GLSPDiagramEditorMarkerUtil.asNavigationTarget(marker, Optional.of(modelState))
            .map(NavigateToTargetAction::new)
            .ifPresent(diagram::dispatch);
      });
   }

   @Override
   public void init(final IEditorSite site, final IEditorInput input) throws PartInitException {
      validateEditorInput(input);
      setSite(site);
      setInput(input);

      IEclipseContext context = site.getService(IEclipseContext.class);

      diagram = createGLSPDiagramComposite();
      diagram.init(context, getFilePath());

      diagram.getModelStateOnceInitialized().thenAccept(this::syncMarkers);
      diagram.addDirtyStateListener(dirty -> {
         setDirty(dirty);
      });
   }

   protected GLSPDiagramComposite createGLSPDiagramComposite() {
      return new GLSPDiagramComposite(getEditorId());
   }

   protected void validateEditorInput(final IEditorInput editorInput) throws PartInitException {
      if (!(editorInput instanceof IFileEditorInput)) {
         throw new PartInitException("Invalid editor input: Must be IFileEditorInput");
      }
   }

   protected String getFilePath() {
      return Optional.ofNullable(getFile())
         .map(IFile::getLocationURI)
         .map(URI::getPath)
         .orElse("");
   }

   protected String getFileName() { return Optional.ofNullable(getFile())
      .map(IFile::getName)
      .orElse(""); }

   protected IFile getFile() {
      IEditorInput editorInput = getEditorInput();
      if (editorInput instanceof IFileEditorInput) {
         return ((IFileEditorInput) editorInput).getFile();
      }
      return null;
   }

   @Override
   public void createPartControl(final Composite parent) {

      diagram.createPartControl(parent);

      setPartName(generatePartName());

      getSite().setSelectionProvider(diagram);

      createBrowserMenu();
   }

   protected void createBrowserMenu() {
      MenuManager menuManager = new MenuManager();
      Menu menu = menuManager.createContextMenu(diagram.getBrowser());
      getSite().registerContextMenu(menuManager, getSite().getSelectionProvider());
      diagram.getBrowser().setMenu(menu);
   }

   protected String generatePartName() {
      return getFileName();
   }

   protected void syncMarkers(final GModelState modelState) {
      IdeClientOptions.getSourceUriAsIFile(modelState.getClientOptions())
         .map(workspaceFile -> GLSPDiagramEditorMarkerUtil.syncMarkers(
            workspaceFile,
            modelState.getClientId(),
            diagram.getActionDispatcher().getNow(null)))
         .ifPresent(toDispose::add);
   }

   @Override
   public void setFocus() {
      diagram.setFocus();
   }

   public void notifyAboutToBeDisposed() {
      // we are about to be disposed. don't send and accept actions anymore
      diagram.notifyAboutToBeDisposed();
   }

   @Override
   public void dispose() {
      diagram.dispose();
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

   protected static class SelectionManager extends EventManager {

      public void addSelectionChangedListener(final ISelectionChangedListener listener) {
         addListenerObject(listener);
      }

      public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
         removeListenerObject(listener);
      }

      public void selectionChanged(final SelectionChangedEvent event) {
         for (Object listener : getListeners()) {
            final ISelectionChangedListener selectionChangedListeners = (ISelectionChangedListener) listener;
            UIUtil.asyncExec(() -> selectionChangedListeners.selectionChanged(event));
         }
      }

   }

   @Override
   public void addSelectionChangedListener(final ISelectionChangedListener listener) {
      selectionListener.addSelectionChangedListener(listener);
   }

   @Override
   public void removeSelectionChangedListener(final ISelectionChangedListener listener) {
      selectionListener.removeSelectionChangedListener(listener);
   }

   @Override
   public ISelection getSelection() { return diagram.getSelection(); }

   @Override
   public void setSelection(final ISelection selection) {
      diagram.setSelection(selection);
   }

   public void updateSelection(final SelectAction selectAction) {
      diagram.updateSelection(selectAction);
   }
}

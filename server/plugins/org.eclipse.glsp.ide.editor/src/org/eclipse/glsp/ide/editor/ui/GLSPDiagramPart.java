/********************************************************************************
 * Copyright (c) 2023 EclipseSource and others.
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

import java.util.Map.Entry;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Inject;

import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MPopupMenu;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.glsp.server.actions.SaveModelAction;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.handlers.IActionCommandMappingService;

@SuppressWarnings("restriction")
public abstract class GLSPDiagramPart {

   protected final GLSPDiagramComposite diagram;

   @Inject
   protected MPart part;

   protected GLSPDiagramPart(final String glspEditorId) {
      diagram = new GLSPDiagramComposite(glspEditorId);
      GLSPIdeEditorPlugin.getDefault().getGLSPEditorRegistry().registerComposite(diagram);
   }

   @PostConstruct
   public void createContent(final Composite composite, final IEclipseContext context,
      final ESelectionService selectionService, final EHandlerService handlerService, final EMenuService menuService) {

      diagram.init(context, getInput());
      diagram.addDirtyStateListener(this::setDirty);
      diagram.createPartControl(composite);
      diagram.addSelectionChangedListener(event -> selectionService.setSelection(diagram.getSelection()));

      initilizeCommands(context, handlerService);
      registerContextMenu(menuService);
   }

   protected boolean registerContextMenu(final EMenuService menuService) {
      for (MMenu menu : part.getMenus()) {
         if (menu instanceof MPopupMenu) {
            return menuService.registerContextMenu(diagram.getBrowser(), menu.getElementId());
         }
      }
      return false;
   }

   protected void initilizeCommands(final IEclipseContext context, final EHandlerService handlerService) {
      for (Entry<String, IAction> action : diagram.getGlobalActions().entrySet()) {
         IActionCommandMappingService commandMappingService = context.get(IActionCommandMappingService.class);
         String commandId = commandMappingService.getCommandId(action.getKey());
         handlerService.activateHandler(commandId, new Handler(action.getValue()));
      }
   }

   protected abstract String getInput();

   protected static class Handler {

      private final IAction value;

      Handler(final IAction value) {
         this.value = value;
      }

      @Execute
      public void execute() {
         value.run();
      }
   }

   protected void setDirty(final boolean dirty) {
      part.setDirty(dirty);
   }

   @Focus
   public void focus() {
      diagram.setFocus();
   }

   @PreDestroy
   public void preDestroy() {
      diagram.notifyAboutToBeDisposed();
      diagram.dispose();
   }

   @Persist
   public void save() {
      diagram.dispatch(new SaveModelAction());
   }

   public MPart getPart() { return part; }
}

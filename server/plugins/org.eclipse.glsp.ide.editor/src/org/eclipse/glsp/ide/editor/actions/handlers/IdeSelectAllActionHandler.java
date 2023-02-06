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
package org.eclipse.glsp.ide.editor.actions.handlers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.glsp.ide.editor.ui.GLSPIdeEditorPlugin;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.SelectAction;
import org.eclipse.glsp.server.actions.SelectAllAction;
import org.eclipse.glsp.server.model.GModelState;

import com.google.inject.Inject;

/**
 * Forwards the selection of an {@link SelectAllAction} to the current editor to update its
 * selection state.
 */
public class IdeSelectAllActionHandler extends AbstractActionHandler<SelectAllAction> {

   @Inject
   protected GModelState modelState;

   @Override
   protected List<Action> executeAction(final SelectAllAction selectAllAction) {
      SelectAction selectAction;
      if (selectAllAction.isSelect()) {
         // We don't know on the server which elements are selectable so we just put all
         // elements of the diagram into the selection state.
         selectAction = new SelectAction(new ArrayList<>(modelState.getIndex().allIds()));
      } else {
         selectAction = new SelectAction(Collections.emptyList(), new ArrayList<>(modelState.getIndex().allIds()));
      }
      GLSPIdeEditorPlugin.getDefaultGLSPEditorRegistry().getGLSPEditorOrThrow(modelState.getClientId())
         .updateSelection(selectAction);
      return none();
   }

}

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
package org.eclipse.glsp.ide.workflow.editor;

import java.util.List;

import org.eclipse.glsp.graph.GModelRoot;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.features.core.model.RequestModelAction;
import org.eclipse.glsp.server.features.core.model.RequestModelActionHandler;
import org.eclipse.glsp.server.utils.MapUtil;

public class WorkflowRequestModelActionHandler extends RequestModelActionHandler {

   @Override
   public List<Action> executeAction(final RequestModelAction action) {

      boolean isReconnecting = MapUtil.getBoolValue(action.getOptions(), "isReconnecting");

      modelState.setClientOptions(action.getOptions());

      notifyStartLoading();
      if (isReconnecting) {
         GModelRoot oldModel = modelState.getRoot();
         // use current modelRoot of modelState and submit
         modelState.updateRoot(oldModel);
         // decrease revision by one, as each submit will increase it by one; the next save will produce warning that
         // source model was changed otherwise
         modelState.getRoot().setRevision(oldModel.getRevision() - 1);
      } else {
         sourceModelStorage.loadSourceModel(action);
      }
      notifyFinishedLoading();

      sourceModelWatcher.ifPresent(watcher -> watcher.startWatching());

      return modelSubmissionHandler.submitModel();
   }

}

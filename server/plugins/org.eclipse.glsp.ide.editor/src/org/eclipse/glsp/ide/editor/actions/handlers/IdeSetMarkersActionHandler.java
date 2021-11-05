/********************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
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

import static org.eclipse.glsp.server.types.GLSPServerException.getOrThrow;

import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.glsp.ide.editor.utils.GLSPDiagramEditorMarkerUtil;
import org.eclipse.glsp.ide.editor.utils.IdeClientOptions;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.features.validation.SetMarkersAction;
import org.eclipse.glsp.server.model.GModelState;

import com.google.inject.Inject;

public class IdeSetMarkersActionHandler extends AbstractActionHandler<SetMarkersAction> {

   @Inject
   protected GModelState modelState;

   @Override
   protected List<Action> executeAction(final SetMarkersAction action) {
      final IFile workspaceFile = getOrThrow(IdeClientOptions.getSourceUriAsIFile(modelState.getClientOptions()),
         "Could not retrieve model source URL for: " + modelState.getClientId());
      GLSPDiagramEditorMarkerUtil.clearMarkers(workspaceFile, false);
      action.getMarkers().forEach(glspMarker -> GLSPDiagramEditorMarkerUtil.createMarker(workspaceFile, glspMarker));
      return none();
   }

}

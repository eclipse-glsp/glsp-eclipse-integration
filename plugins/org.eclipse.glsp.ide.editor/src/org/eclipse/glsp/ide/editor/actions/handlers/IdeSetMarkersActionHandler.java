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
package org.eclipse.glsp.ide.editor.actions.handlers;

import static org.eclipse.glsp.server.protocol.GLSPServerException.getOrThrow;

import java.net.URI;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.glsp.ide.editor.utils.GLSPDiagramEditorMarkerUtil;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.BasicActionHandler;
import org.eclipse.glsp.server.features.validation.SetMarkersAction;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.utils.ClientOptions;

public class IdeSetMarkersActionHandler extends BasicActionHandler<SetMarkersAction> {

   @Override
   protected List<Action> executeAction(final SetMarkersAction action, final GModelState modelState) {
      URI modelURI = getOrThrow(
         ClientOptions.getValue(modelState.getClientOptions(), ClientOptions.SOURCE_URI).map(URI::create),
         "Could not retrieve model source URL for: " + modelState.getClientId());
      for (IFile file : ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(modelURI)) {
         GLSPDiagramEditorMarkerUtil.clearMarkers(file);
         action.getMarkers().forEach(glspMarker -> GLSPDiagramEditorMarkerUtil.createMarker(file, glspMarker));
      }
      return none();
   }

}

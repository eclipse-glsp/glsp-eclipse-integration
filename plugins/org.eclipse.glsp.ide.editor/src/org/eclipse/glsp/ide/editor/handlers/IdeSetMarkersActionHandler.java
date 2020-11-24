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
package org.eclipse.glsp.ide.editor.handlers;

import static org.eclipse.glsp.server.protocol.GLSPServerException.getOrThrow;

import java.net.URI;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.actions.BasicActionHandler;
import org.eclipse.glsp.server.features.validation.Marker;
import org.eclipse.glsp.server.features.validation.MarkerKind;
import org.eclipse.glsp.server.features.validation.SetMarkersAction;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.utils.ClientOptions;

public class IdeSetMarkersActionHandler extends BasicActionHandler<SetMarkersAction> {
   public static final String GLSP_MARKER = "org.eclipse.glsp.ide.marker.problem";
   private static final Logger LOGGER = Logger.getLogger(IdeSetMarkersActionHandler.class);

   @Override
   protected List<Action> executeAction(final SetMarkersAction action, final GModelState modelState) {
      URI modelURI = getOrThrow(
         ClientOptions.getValue(modelState.getClientOptions(), ClientOptions.SOURCE_URI).map(URI::create),
         "Could not retrieve model source URL for: " + modelState.getClientId());
      for (IFile file : ResourcesPlugin.getWorkspace().getRoot().findFilesForLocationURI(modelURI)) {
         clearMarkers(file);
         action.getMarkers().forEach(glspMarker -> createMarker(file, glspMarker));
      }
      return none();
   }

   protected void clearMarkers(final IFile file) {
      try {
         for (IMarker toDelete : file.findMarkers(GLSP_MARKER, false, IResource.DEPTH_ONE)) {
            toDelete.delete();
         }
      } catch (CoreException e) {
         LOGGER.error(e);
         throw new GLSPServerException("Could not clear markers for: " + file.getLocationURI(), e);
      }
   }

   protected void createMarker(final IFile file, final Marker glspMarker) {
      IMarker marker;
      try {
         marker = file.createMarker(GLSP_MARKER);
         marker.setAttribute(IMarker.MESSAGE, glspMarker.getLabel());
         marker.setAttribute(IMarker.LOCATION, glspMarker.getElementId());
         marker.setAttribute(IMarker.SEVERITY, toIMarkerSeverity(glspMarker));
         marker.setAttribute(IMarker.PRIORITY, toIMarkerPriority(glspMarker));
      } catch (CoreException e) {
         LOGGER.error(e);
         throw new GLSPServerException("Error during marker creation for: " + file.getLocationURI(), e);
      }
   }

   public static int toIMarkerSeverity(final Marker glspMarker) {
      switch (glspMarker.getType()) {
         case MarkerKind.INFO:
            return IMarker.SEVERITY_INFO;
         case MarkerKind.WARNING:
            return IMarker.SEVERITY_WARNING;
         default:
            return IMarker.SEVERITY_ERROR;
      }
   }

   public static int toIMarkerPriority(final Marker glspMarker) {
      switch (glspMarker.getType()) {
         case MarkerKind.INFO:
            return IMarker.PRIORITY_LOW;
         case MarkerKind.WARNING:
            return IMarker.PRIORITY_NORMAL;
         default:
            return IMarker.PRIORITY_HIGH;
      }
   }

}

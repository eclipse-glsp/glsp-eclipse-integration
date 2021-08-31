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
package org.eclipse.glsp.ide.editor.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.glsp.server.actions.ActionDispatcher;
import org.eclipse.glsp.server.disposable.IDisposable;
import org.eclipse.glsp.server.features.navigation.NavigationTarget;
import org.eclipse.glsp.server.features.validation.DeleteMarkersAction;
import org.eclipse.glsp.server.features.validation.Marker;
import org.eclipse.glsp.server.features.validation.MarkerKind;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.protocol.GLSPServerException;
import org.eclipse.glsp.server.utils.ClientOptions;
import org.eclipse.ui.texteditor.MarkerUtilities;

public final class GLSPDiagramEditorMarkerUtil {
   public static final String GLSP_MARKER = "org.eclipse.glsp.ide.marker.problem";

   private static final Logger LOGGER = Logger.getLogger(GLSPDiagramEditorMarkerUtil.class);
   private static final String ATTRIBUTE_GLSP_MARKER = "glspMarker";
   private static final String ATTRIBUTE_SYNC_WITH_CLIENT = "syncWithClient";

   private GLSPDiagramEditorMarkerUtil() {}

   public static IMarker createMarker(final IResource resource, final Marker glspMarker) {
      try {
         IMarker marker = resource.createMarker(GLSP_MARKER);
         marker.setAttribute(IMarker.MESSAGE, glspMarker.getLabel());
         marker.setAttribute(IMarker.LOCATION, glspMarker.getElementId());
         marker.setAttribute(IMarker.SEVERITY, toIMarkerSeverity(glspMarker));
         marker.setAttribute(IMarker.PRIORITY, toIMarkerPriority(glspMarker));
         marker.setAttribute(ATTRIBUTE_GLSP_MARKER, glspMarker);
         return marker;
      } catch (CoreException exception) {
         LOGGER.error(exception);
         throw new GLSPServerException("Error during marker creation for: " + resource.getLocationURI(), exception);
      }
   }

   public static Optional<NavigationTarget> asNavigationTarget(final IMarker marker,
      final Optional<GModelState> modelState) {
      if (modelState.isEmpty() || !GLSP_MARKER.equals(MarkerUtilities.getMarkerType(marker))) {
         return Optional.empty();
      }
      String elementId = marker.getAttribute(IMarker.LOCATION, null);
      if (elementId == null) {
         return Optional.empty();
      }
      NavigationTarget target = new NavigationTarget(
         ClientOptions.getSourceUri(modelState.get().getClientOptions()).orElseThrow(),
         MarkerUtilities.getMessage(marker),
         new HashMap<String, String>());
      target.setElementIds(Collections.singletonList(elementId));

      return Optional.of(target);
   }

   public static void clearMarkers(final IResource resource, final boolean syncWithClient) {
      try {
         for (IMarker toDelete : resource.findMarkers(GLSP_MARKER, false, IResource.DEPTH_ONE)) {
            toDelete.setAttribute(ATTRIBUTE_SYNC_WITH_CLIENT, syncWithClient);
            toDelete.delete();
         }
      } catch (CoreException exception) {
         LOGGER.error(exception);
         throw new GLSPServerException("Could not clear markers for: " + resource.getLocationURI(), exception);
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

   public static IDisposable syncMarkers(final IResource resource, final String clientId,
      final ActionDispatcher actionDispatcher) {
      MarkerDeletionListener listener = new MarkerDeletionListener(resource, clientId, actionDispatcher);
      ResourcesPlugin.getWorkspace().addResourceChangeListener(listener);
      return IDisposable.create(() -> ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener));
   }

   private static class MarkerDeletionListener implements IResourceChangeListener {

      private final IResource resource;
      private final String clientId;
      private final ActionDispatcher actionDispatcher;

      MarkerDeletionListener(final IResource resource, final String clientId, final ActionDispatcher actionDispatcher) {
         this.resource = resource;
         this.clientId = clientId;
         this.actionDispatcher = actionDispatcher;
      }

      @Override
      public void resourceChanged(final IResourceChangeEvent event) {
         try {
            event.getDelta().accept(new MarkerDeletionVisitor(this.resource, this.clientId, this.actionDispatcher));
         } catch (CoreException exception) {
            LOGGER.error(exception);
         }
      }
   }

   private static class MarkerDeletionVisitor implements IResourceDeltaVisitor {
      private final IResource resource;
      private final String clientId;
      private final ActionDispatcher actionDispatcher;

      MarkerDeletionVisitor(final IResource resource, final String clientId, final ActionDispatcher actionDispatcher) {
         this.resource = resource;
         this.clientId = clientId;
         this.actionDispatcher = actionDispatcher;
      }

      @Override
      public boolean visit(final IResourceDelta resourceDelta) throws CoreException {
         if (resourceDelta == null) {
            return false;
         }
         if (this.resource.equals(resourceDelta.getResource())) {
            Arrays.stream(resourceDelta.getMarkerDeltas())
               .filter(delta -> delta.isSubtypeOf(GLSP_MARKER) && delta.getKind() == IResourceDelta.REMOVED)
               .forEach(this::handleMarkerDeleted);
            return false;
         }
         return true;
      }

      private void handleMarkerDeleted(final IMarkerDelta markerDelta) {
         boolean sync = markerDelta.getAttribute(ATTRIBUTE_SYNC_WITH_CLIENT, true);
         if (!sync) {
            return;
         }
         Object glspMarker = markerDelta.getAttribute(ATTRIBUTE_GLSP_MARKER);
         if (glspMarker instanceof Marker) {
            this.actionDispatcher.dispatch(this.clientId,
               new DeleteMarkersAction(Collections.singletonList((Marker) glspMarker)));
         }
      }

   }
}

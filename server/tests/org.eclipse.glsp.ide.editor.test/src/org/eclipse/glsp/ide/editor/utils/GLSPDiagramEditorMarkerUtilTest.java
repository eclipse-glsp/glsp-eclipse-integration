/********************************************************************************
 * Copyright (c) 2026 EclipseSource and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.core.resources.IMarker;
import org.eclipse.glsp.server.features.validation.Marker;
import org.eclipse.glsp.server.features.validation.MarkerKind;
import org.junit.jupiter.api.Test;

class GLSPDiagramEditorMarkerUtilTest {

   @Test
   void mapsInfoMarkers() {
      Marker marker = marker(MarkerKind.INFO);

      assertEquals(IMarker.SEVERITY_INFO, GLSPDiagramEditorMarkerUtil.toIMarkerSeverity(marker));
      assertEquals(IMarker.PRIORITY_LOW, GLSPDiagramEditorMarkerUtil.toIMarkerPriority(marker));
   }

   @Test
   void mapsWarningMarkers() {
      Marker marker = marker(MarkerKind.WARNING);

      assertEquals(IMarker.SEVERITY_WARNING, GLSPDiagramEditorMarkerUtil.toIMarkerSeverity(marker));
      assertEquals(IMarker.PRIORITY_NORMAL, GLSPDiagramEditorMarkerUtil.toIMarkerPriority(marker));
   }

   @Test
   void mapsErrorAndUnknownMarkersToError() {
      for (String kind : new String[] { MarkerKind.ERROR, "unknown" }) {
         Marker marker = marker(kind);

         assertEquals(IMarker.SEVERITY_ERROR, GLSPDiagramEditorMarkerUtil.toIMarkerSeverity(marker), kind);
         assertEquals(IMarker.PRIORITY_HIGH, GLSPDiagramEditorMarkerUtil.toIMarkerPriority(marker), kind);
      }
   }

   static Marker marker(final String kind) {
      return new Marker("label", "description", "element", kind);
   }
}

/********************************************************************************
 * Copyright (c) 2021 EclipseSource and others.
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
package org.eclipse.glsp.ide.editor.internal.actions;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.glsp.graph.GSeverity;
import org.eclipse.glsp.server.actions.ResponseAction;

public class NavigateToMarkerAction extends ResponseAction {
   public static final String ID = "navigateToMarker";
   public static final List<String> ALL_SEVERITIES = GSeverity.VALUES.stream().map(GSeverity::toString)
      .collect(Collectors.toList());

   public enum Direction {
      NEXT,
      PREVIOUS;
   }

   private String direction;
   private List<String> selectedElementIds;
   private List<String> severities;

   public NavigateToMarkerAction() {
      super(ID);
   }

   public NavigateToMarkerAction(final List<String> selectedElementsIDs) {
      this(Direction.NEXT, selectedElementsIDs, ALL_SEVERITIES);
   }

   public NavigateToMarkerAction(final Direction direction, final List<String> selectedElementsIDs) {
      this(direction, selectedElementsIDs, ALL_SEVERITIES);
   }

   public NavigateToMarkerAction(final Direction direction, final List<String> selectedElementsIDs,
      final List<String> severities) {
      super(ID);
      this.direction = direction.name().toLowerCase();
      this.selectedElementIds = selectedElementsIDs;
      this.severities = severities;
   }

   public String getDirection() { return direction; }

   public void setDirection(final String direction) { this.direction = direction; }

   public Optional<List<String>> getSelectedElementIds() { return Optional.ofNullable(selectedElementIds); }

   public void setSelectedElementIds(final List<String> selectedElementIds) {
      this.selectedElementIds = selectedElementIds;
   }

   public List<String> getSeverities() { return severities; }

   public void setSeverities(final List<String> severities) { this.severities = severities; }

}

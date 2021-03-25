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

import org.eclipse.glsp.server.actions.Action;

public class ModifyCSSFeedbackAction extends Action {
   public static final String ID = "modifyCSSFeedback";

   private List<String> elementsIDs;
   private List<String> addClasses;
   private List<String> removeClasses;

   public ModifyCSSFeedbackAction() {
      super(ID);
   }

   public ModifyCSSFeedbackAction(final List<String> elementsIDs, final List<String> addClasses,
      final List<String> removeClasses) {
      super(ID);
      this.elementsIDs = elementsIDs;
      this.addClasses = addClasses;
      this.removeClasses = removeClasses;
   }

   public Optional<List<String>> getElementsIDs() { return Optional.ofNullable(elementsIDs); }

   public void setElementsIDs(final List<String> elementsIDs) { this.elementsIDs = elementsIDs; }

   public Optional<List<String>> getAddClasses() { return Optional.ofNullable(addClasses); }

   public void setAddClasses(final List<String> addClasses) { this.addClasses = addClasses; }

   public Optional<List<String>> getRemoveClasses() { return Optional.ofNullable(removeClasses); }

   public void setRemoveClasses(final List<String> removeClasses) { this.removeClasses = removeClasses; }
}

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
package org.eclipse.glsp.ide.editor.initialization;

import java.util.Objects;

import org.eclipse.glsp.ide.editor.actions.InitializeCanvasBoundsAction;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.features.core.model.SetModelAction;

/**
 * Default initialization constraint triggers after a non-empty UpdateModelAction and a subsequent
 * InitializeCanvasBoundsAction.
 */
public class DefaultModelInitializationConstraint extends AbstractModelInitializationConstraint {

   private boolean nonEmptySetModelActionDispatched;

   @Override
   protected boolean isInitializedAfter(final Action action) {
      nonEmptySetModelActionDispatched = nonEmptySetModelActionDispatched || isNotEmptySetModelAction(action);
      return nonEmptySetModelActionDispatched && isInitializeCanvasBoundsAction(action);
   }

   private static boolean isNotEmptySetModelAction(final Action action) {
      return action instanceof SetModelAction
         && ((SetModelAction) action).getNewRoot() != null
         && !Objects.equals(((SetModelAction) action).getNewRoot().getType(), "NONE");
   }

   private static boolean isInitializeCanvasBoundsAction(final Action action) {
      return action instanceof InitializeCanvasBoundsAction;
   }
}

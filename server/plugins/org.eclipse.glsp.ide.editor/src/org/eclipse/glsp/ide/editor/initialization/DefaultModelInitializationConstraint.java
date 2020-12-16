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
import org.eclipse.glsp.server.features.core.model.UpdateModelAction;

/**
 * Default initialization constraint triggers after a non-empty UpdateModelAction and a subsequent
 * InitializeCanvasBoundsAction.
 */
public class DefaultModelInitializationConstraint extends AbstractModelInitializationConstraint {

   private boolean nonEmptyUpdateModelActionDispatched;

   @Override
   protected boolean isInitializedAfter(final Action action) {
      nonEmptyUpdateModelActionDispatched = nonEmptyUpdateModelActionDispatched || isNonEmptyUpdateModelAction(action);
      return nonEmptyUpdateModelActionDispatched && isInitializeCanvasBoundsAction(action);
   }

   private static boolean isNonEmptyUpdateModelAction(final Action action) {
      return action instanceof UpdateModelAction
         && ((UpdateModelAction) action).getNewRoot() != null
         && !Objects.equals(((UpdateModelAction) action).getNewRoot().getType(), "NONE");
   }

   private static boolean isInitializeCanvasBoundsAction(final Action action) {
      return action instanceof InitializeCanvasBoundsAction;
   }
}

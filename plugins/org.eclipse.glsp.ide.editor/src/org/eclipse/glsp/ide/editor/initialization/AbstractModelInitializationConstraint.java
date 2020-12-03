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

import java.util.concurrent.CompletableFuture;

import org.eclipse.glsp.server.actions.Action;

public abstract class AbstractModelInitializationConstraint implements ModelInitializationConstraint {
   private final CompletableFuture<Void> initialized = new CompletableFuture<>();
   private boolean completed;

   @Override
   public CompletableFuture<Void> onInitialized() {
      return initialized;
   }

   public boolean isCompleted() { return completed; }

   @Override
   public void notifyDispatched(final Action action) {
      if (isCompleted()) {
         return;
      }
      if (isInitializedAfter(action)) {
         setCompleted(true);
      }
   }

   protected void setCompleted(final boolean completed) {
      this.completed = completed;
      if (completed) {
         this.initialized.complete(null);
      }
   }

   protected abstract boolean isInitializedAfter(Action action);
}

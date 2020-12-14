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
package org.eclipse.glsp.ide.editor.actions;

import org.eclipse.glsp.graph.GBounds;
import org.eclipse.glsp.server.actions.Action;

/**
 * Sprotty's InitializeCanvasBoundsAction that is sent after the canvas bounds have been initialized.
 */
public class InitializeCanvasBoundsAction extends Action {
   public static final String ID = "initializeCanvasBounds";

   private GBounds newCanvasBounds;

   public InitializeCanvasBoundsAction() {
      super(ID);
   }

   public GBounds getNewCanvasBounds() { return newCanvasBounds; }

   public void setNewCanvasBounds(final GBounds newCanvasBounds) { this.newCanvasBounds = newCanvasBounds; }

}

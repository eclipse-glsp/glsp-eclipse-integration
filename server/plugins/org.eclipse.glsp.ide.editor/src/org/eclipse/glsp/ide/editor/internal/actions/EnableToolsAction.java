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

import org.eclipse.glsp.server.actions.Action;

public class EnableToolsAction extends Action {
   public static final String ID = "enable-tools";

   private List<String> toolIds;

   public EnableToolsAction() {
      super(ID);
   }

   public EnableToolsAction(final List<String> toolIds) {
      super(ID);
      this.toolIds = toolIds;
   }

   public List<String> getToolIds() { return toolIds; }

   public void setToolIds(final List<String> toolIds) { this.toolIds = toolIds; }
}

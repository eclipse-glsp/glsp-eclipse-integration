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

import java.util.Map;

import org.eclipse.glsp.server.actions.Action;

public class NavigateAction extends Action {
   public static final String ID = "navigate";

   private String targetTypeId;
   private Map<String, String> args;

   public NavigateAction() {
      super(ID);
   }

   public NavigateAction(final String targetTypeId) {
      this();
      this.targetTypeId = targetTypeId;
   }

   public NavigateAction(final String targetTypeId, final Map<String, String> args) {
      this();
      this.targetTypeId = targetTypeId;
      this.args = args;
   }

   public String getTargetTypeId() { return targetTypeId; }

   public void setTargetTypeId(final String targetTypeId) { this.targetTypeId = targetTypeId; }

   public Map<String, String> getArgs() { return args; }

   public void setArgs(final Map<String, String> args) { this.args = args; }

}

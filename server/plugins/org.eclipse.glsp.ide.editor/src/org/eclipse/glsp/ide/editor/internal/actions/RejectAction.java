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

import java.util.Optional;

import org.eclipse.glsp.server.actions.ResponseAction;

public class RejectAction extends ResponseAction {
   public static final String ID = "rejectRequest";

   private String message;
   private Object detail;

   public RejectAction() {
      super(ID);
   }

   public RejectAction(final String message) {
      this(message, null);
   }

   public RejectAction(final String message, final Object detail) {
      super(ID);
      this.message = message;
      this.detail = detail;
   }

   public String getMessage() { return message; }

   public void setMessage(final String message) { this.message = message; }

   public Optional<Object> getDetail() { return Optional.ofNullable(detail); }

   public void setDetail(final Object detail) { this.detail = detail; }
}

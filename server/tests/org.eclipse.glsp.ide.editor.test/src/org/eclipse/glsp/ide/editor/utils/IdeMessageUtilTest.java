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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.glsp.server.actions.MessageAction;
import org.eclipse.glsp.server.types.Severity;
import org.junit.jupiter.api.Test;

class IdeMessageUtilTest {

   @Test
   void appendsDetailsToMessage() {
      IStatus status = IdeMessageUtil.toStatus(new MessageAction(Severity.ERROR, "message", "details")).orElseThrow();

      assertEquals("message\ndetails", status.getMessage());
      assertEquals(IStatus.ERROR, status.getSeverity());
   }

   @Test
   void usesMessageWithoutDetails() {
      IStatus status = IdeMessageUtil.toStatus(new MessageAction(Severity.WARNING, "message")).orElseThrow();

      assertEquals("message", status.getMessage());
   }

   @Test
   void usesDetailsWithoutMessage() {
      IStatus status = IdeMessageUtil.toStatus(new MessageAction(Severity.INFO, null, "details")).orElseThrow();

      assertEquals("details", status.getMessage());
   }

   @Test
   void createsNoStatusWithoutMessageAndDetails() {
      assertTrue(IdeMessageUtil.toStatus(new MessageAction(Severity.INFO, "")).isEmpty());
   }

   @Test
   void mapsSeverities() {
      assertEquals(IStatus.OK, IdeMessageUtil.toSeverity("NONE"));
      assertEquals(IStatus.OK, IdeMessageUtil.toSeverity("OK"));
      assertEquals(IStatus.INFO, IdeMessageUtil.toSeverity("INFO"));
      assertEquals(IStatus.WARNING, IdeMessageUtil.toSeverity("WARNING"));
      assertEquals(IStatus.ERROR, IdeMessageUtil.toSeverity("ERROR"));
      assertEquals(IStatus.ERROR, IdeMessageUtil.toSeverity("FATAL"));
   }

   @Test
   void parsesSeverities() {
      assertEquals(Severity.WARNING, IdeMessageUtil.parseSeverity("WARNING"));
      assertEquals(Severity.NONE, IdeMessageUtil.parseSeverity(null));
      assertEquals(Severity.NONE, IdeMessageUtil.parseSeverity("unknown"));
   }

   @Test
   void mapsMissingOrUnknownSeverityToOk() {
      assertEquals(IStatus.OK, IdeMessageUtil.toSeverity(null));
      assertEquals(IStatus.OK, IdeMessageUtil.toSeverity("unknown"));
   }
}

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

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Collections;
import java.util.List;

import org.eclipse.glsp.server.actions.Action;

public class LoggingAction extends Action {
   public static final String ID = "logging";

   public enum Severity {
      NONE,
      ERROR,
      WARN,
      INFO,
      LOG
   }

   private String severity;
   private String time;
   private String caller;
   private String message;
   private List<String> params;

   public LoggingAction() {
      super(ID);
   }

   public LoggingAction(final Severity severity, final String message) {
      this(severity, LoggingAction.class.getSimpleName(), message, Collections.emptyList());
   }

   public LoggingAction(final Severity severity, final String caller, final String message) {
      this(severity, caller, message, Collections.emptyList());
   }

   public LoggingAction(final Severity severity, final String caller, final String message, final List<String> params) {
      this(severity, LocalTime.now().format(DateTimeFormatter.ofLocalizedTime(FormatStyle.LONG)), caller, message,
         params);
   }

   public LoggingAction(final Severity severity, final String time, final String caller, final String message,
      final List<String> params) {
      super(ID);
      this.severity = severity.name().toLowerCase();
      this.time = time;
      this.caller = caller;
      this.message = message;
      this.params = params;
   }

   public String getSeverity() { return severity; }

   public void setSeverity(final String severity) { this.severity = severity; }

   public String getTime() { return time; }

   public void setTime(final String time) { this.time = time; }

   public String getCaller() { return caller; }

   public void setCaller(final String caller) { this.caller = caller; }

   public String getMessage() { return message; }

   public void setMessage(final String message) { this.message = message; }

   public List<String> getParams() { return params; }

   public void setParams(final List<String> params) { this.params = params; }
}

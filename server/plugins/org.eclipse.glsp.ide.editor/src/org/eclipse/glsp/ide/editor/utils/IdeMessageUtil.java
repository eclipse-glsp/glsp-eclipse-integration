/********************************************************************************
 * Copyright (c) 2020-2026 EclipseSource and others.
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

import java.util.Optional;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.ide.editor.di.IdeActionDispatcher;
import org.eclipse.glsp.server.actions.MessageAction;
import org.eclipse.glsp.server.types.Severity;
import org.eclipse.jface.dialogs.ErrorDialog;

public final class IdeMessageUtil {
   private IdeMessageUtil() {}

   public static void log(final MessageAction action, final String clientId) {
      Optional<IStatus> status = toStatus(action);
      if (status.isEmpty()) {
         return;
      }
      Platform.getLog(IdeMessageUtil.class).log(status.get());
      if (status.get().getSeverity() >= IStatus.ERROR) {
         UIUtil.asyncExec(() -> {
            UIUtil.findShell(clientId).ifPresent(
               shell -> ErrorDialog.openError(shell, "GLSP Server Error", action.getMessage(), status.get()));
         });
      }
   }

   public static Optional<IStatus> toStatus(final MessageAction action) {
      String message = Optional.ofNullable(action.getMessage()).orElse("");
      String details = action.getDetails().orElse("");
      if (message.isEmpty()) {
         message = details;
      } else if (!details.isEmpty()) {
         message = message + "\n" + details;
      }
      if (message.isEmpty()) {
         return Optional.empty();
      }
      return Optional.of(new Status(toSeverity(action.getSeverity()), IdeActionDispatcher.class, message));
   }

   public static int toSeverity(final String glspSeverity) {
      return switch (parseSeverity(glspSeverity)) {
         case INFO -> IStatus.INFO;
         case WARNING -> IStatus.WARNING;
         case ERROR, FATAL -> IStatus.ERROR;
         default -> IStatus.OK;
      };
   }

   /** Parses a GLSP severity string, returning {@link Severity#NONE} for missing or unknown values. */
   public static Severity parseSeverity(final String glspSeverity) {
      if (glspSeverity == null) {
         return Severity.NONE;
      }
      try {
         return Severity.valueOf(glspSeverity);
      } catch (IllegalArgumentException exception) {
         return Severity.NONE;
      }
   }
}

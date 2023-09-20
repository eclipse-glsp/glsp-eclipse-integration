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
      int severity = toSeverity(action.getSeverity());
      String message = action.getMessage();
      if (message == null || message.isEmpty()) {
         message = action.getDetails().orElse("");
      } else if (action.getDetails() != null && !action.getDetails().isEmpty()) {
         message = message + "\n" + action.getDetails();
      }
      if (message != null && !message.isEmpty()) {
         return Optional.of(new Status(severity, IdeActionDispatcher.class, message));
      }
      return Optional.empty();
   }

   public static int toSeverity(final String glspSeverity) {
      switch (Severity.valueOf(glspSeverity)) {
         case NONE:
            return IStatus.OK;
         case OK:
            return IStatus.OK;
         case INFO:
            return IStatus.INFO;
         case WARNING:
            return IStatus.WARNING;
         case ERROR:
            return IStatus.ERROR;
         case FATAL:
            return IStatus.CANCEL;
         default:
            return IStatus.OK;
      }
   }
}

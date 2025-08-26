/********************************************************************************
 * Copyright (c) 2020-2024 EclipseSource and others.
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
package org.eclipse.glsp.ide.editor.ui;

import static org.eclipse.ui.ISharedImages.IMG_OBJS_ERROR_TSK;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_INFO_TSK;
import static org.eclipse.ui.ISharedImages.IMG_OBJS_WARN_TSK;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.glsp.server.actions.StatusAction;
import org.eclipse.glsp.server.types.Severity;
import org.eclipse.glsp.server.utils.StatusActionUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class GLSPDiagramEditorStatusBar extends Composite {
   public static final ISharedImages SHARED_IMAGES = PlatformUI.getWorkbench().getSharedImages();

   protected final Label statusBarIcon;
   protected final Label statusBarMessage;

   protected StatusAction currentStatus;
   protected final Timer statusTimer = new Timer();

   public GLSPDiagramEditorStatusBar(final Composite parent) {
      super(parent, SWT.NO_SCROLL);

      GridData gridData = new GridData();
      gridData.exclude = true;
      setLayoutData(gridData);

      statusBarIcon = new Label(this, SWT.NONE);
      statusBarIcon.setImage(SHARED_IMAGES.getImage(IMG_OBJS_INFO_TSK));

      statusBarMessage = new Label(this, SWT.NONE);
      statusBarMessage.setText("");
   }

   public synchronized void showServerStatus(final StatusAction status) {
      this.currentStatus = status;
      UIUtil.asyncExec(() -> updateStatusBar(status));
      if (status.getTimeout() > 0) {
         int timeout = status.getTimeout();
         statusTimer.schedule(new ClearStatusBarTask(status), timeout);
      }
   }

   protected synchronized void updateStatusBar(final StatusAction status) {
      Severity severity = Severity.valueOf(status.getSeverity());
      Image image = toImage(severity);
      String message = status.getMessage();

      if (image != null && message != null && !message.isEmpty()) {
         statusBarIcon.setImage(image);
         statusBarMessage.setText(message);
         setVisible(true);
         ((GridData) getLayoutData()).exclude = false;
      } else {
         statusBarIcon.setImage(null);
         statusBarMessage.setText("");
         setVisible(false);
         ((GridData) getLayoutData()).exclude = true;
      }
      getParent().layout(true, true);
   }

   private Image toImage(final Severity severity) {
      switch (severity) {
         case FATAL:
         case ERROR:
            return SHARED_IMAGES.getImage(IMG_OBJS_ERROR_TSK);
         case WARNING:
            return SHARED_IMAGES.getImage(IMG_OBJS_WARN_TSK);
         case INFO:
         case OK:
            return SHARED_IMAGES.getImage(IMG_OBJS_INFO_TSK);
         case NONE:
         default:
            return null;
      }
   }

   @Override
   public void dispose() {
      statusTimer.cancel();
      super.dispose();
   }

   protected class ClearStatusBarTask extends TimerTask {
      protected StatusAction startStatus;

      ClearStatusBarTask(final StatusAction startStatus) {
         this.startStatus = startStatus;
      }

      @Override
      public void run() {
         // do not clear if the current status changed in the meantime as that status might have a different timeout
         if (currentStatus == startStatus) {
            UIUtil.asyncExec(() -> updateStatusBar(StatusActionUtil.clear()));
         }
      }
   }
}

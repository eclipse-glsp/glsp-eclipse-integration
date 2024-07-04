/********************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
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
package org.eclipse.glsp.ide.editor.clipboard.ui;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.glsp.ide.editor.clipboard.ClipboardService;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.glsp.server.protocol.GLSPClient;
import org.eclipse.glsp.server.protocol.GLSPServer;
import org.eclipse.glsp.server.protocol.GLSPServerListener;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Display;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * A {@link ClipboardService} based on the SWT System {@link Clipboard}.
 */
@Singleton
public class DisplayClipboardService implements ClipboardService, GLSPServerListener {

   @Inject
   protected Provider<GLSPClient> client;

   protected Clipboard clipboard;

   @Inject
   public DisplayClipboardService(final GLSPServer server) {
      UIUtil.asyncExec(() -> {
         this.clipboard = new Clipboard(Display.getCurrent());
      });
      server.addListener(this);

   }

   @Override
   public Optional<String> getClipboardContents(final String type) {

      // Note: during paste, we only care about the Json Format. While we may
      // also copy to plain text, we only rely on Json for Pasting.
      // Other formats are not supported.

      if (JsonTransfer.APPLICATION_JSON.equals(type)) {
         AtomicReference<Optional<String>> result = new AtomicReference<>();
         Display.getDefault().syncExec(() -> {
            if (Arrays.stream(clipboard.getAvailableTypes())
               .anyMatch(data -> JsonTransfer.getInstance().isSupportedType(data))) {
               result.set(Optional.ofNullable(
                  clipboard.getContents(JsonTransfer.getInstance()))
                  .map(String.class::cast));
            } else {
               result.set(Optional.empty());
            }
         });
         return result.get();
      }

      throw new IllegalArgumentException(
         "The DisplayClipboardService only supports " + JsonTransfer.APPLICATION_JSON + " type");
   }

   @Override
   public void serverShutDown(final GLSPServer glspServer) {
      this.clipboard.dispose();
   }

   @Override
   public void setClipboardContents(final Map<String, String> clipboardData) {
      JsonTransfer json = JsonTransfer.getInstance();
      TextTransfer plainText = TextTransfer.getInstance();

      Object[] data = new String[clipboardData.size()];
      Transfer[] types = new Transfer[clipboardData.size()];
      int i = 0;
      for (Map.Entry<String, String> entry : clipboardData.entrySet()) {
         String type = entry.getKey();
         String value = entry.getValue();
         data[i] = value;
         types[i++] = JsonTransfer.APPLICATION_JSON.equals(type) ? json : plainText;
      }

      UIUtil.asyncExec(() -> clipboard.setContents(data, types));
   }

}

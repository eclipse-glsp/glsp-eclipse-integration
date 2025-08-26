/********************************************************************************
 * Copyright (c) 2023-2024 EclipseSource and others.
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

import java.util.Optional;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

public class BrowserContextMenuInstaller implements BrowserFunctionInstaller {

   public static final String FUNCTION_NAME = "requestContextMenu";
   public static final String FUNCTION_INSTALLER = "document.addEventListener(\"contextmenu\", e => { requestContextMenu(); e.preventDefault(); });";

   @Override
   public Optional<BrowserFunction> install(final Browser browser) {
      BrowserFunction browserFunction = new BrowserFunction(browser, FUNCTION_NAME) {
         @Override
         public Object function(final Object[] arguments) {
            browser.getDisplay().asyncExec(() -> requestContextMenu(browser));
            return null;
         }
      };
      browser.execute(FUNCTION_INSTALLER);
      return Optional.of(browserFunction);
   }

   protected void requestContextMenu(final Browser browser) {
      if (browser.isDisposed()) {
         return;
      }
      browser.getMenu().setEnabled(true);
      browser.getMenu().setVisible(true);
   }
}

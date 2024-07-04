/********************************************************************************
 * Copyright (c) 2020-2023 EclipseSource and others.
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

import java.util.Objects;
import java.util.Optional;

import org.eclipse.glsp.ide.editor.internal.utils.SystemUtils;
import org.eclipse.glsp.ide.editor.utils.SWTUtil;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Control;

public class BrowserFocusControlInstaller implements BrowserFunctionInstaller {

   public static final String FUNCTION_NAME = "mouseDownHappened";
   public static final String FUNCTION_INSTALLER = "document.onmousedown = function(e) {if (!e) {e = window.event;} if (e) {mouseDownHappened();}}";

   @Override
   public Optional<BrowserFunction> install(final Browser browser) {
      return SWTUtil.isChromium(browser) ? doInstall(browser) : Optional.empty();
   }

   protected Optional<BrowserFunction> doInstall(final Browser browser) {
      BrowserFunction browserFunction = new BrowserFunction(browser, FUNCTION_NAME) {
         @Override
         public Object function(final Object[] arguments) {
            browser.getDisplay().asyncExec(() -> setFocus(browser));
            return null;
         }
      };
      browser.execute(FUNCTION_INSTALLER);
      return Optional.of(browserFunction);
   }

   protected void setFocus(final Browser browser) {
      if (browser.isDisposed()) {
         return;
      }
      /* if on windows, we need to force the focus */
      if (SystemUtils.isWindows()) {
         browser.forceFocus();
      } else {
         Control focusControl = browser.getDisplay().getFocusControl();
         if (Objects.equals(focusControl, browser)) {
            return;
         }
         browser.setFocus();
         focusControl = browser.getDisplay().getFocusControl();
         if (focusControl != browser) {
            browser.forceFocus();
         }
      }
   }
}

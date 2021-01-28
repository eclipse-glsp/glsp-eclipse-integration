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
package org.eclipse.glsp.ide.editor.ui;

import java.util.Objects;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.swt.widgets.Control;

public class ChromiumSelectionFunction {

   private static final String FUNCTION_NAME = "mouseDownHappened";

   private static final String INSTALL_FUNCTION = "document.onmousedown = function(e) {if (!e) {e = window.event;} if (e) {mouseDownHappened();}}";

   private BrowserFunction browserFunction;

   public ChromiumSelectionFunction(final GLSPDiagramEditor editor, final Browser browser) {
      browserFunction = new BrowserFunction(browser, FUNCTION_NAME) {
         @Override
         public Object function(final Object[] arguments) {
            browser.getDisplay().asyncExec(() -> {
               /* if on windows, we need to force the focus */
               String osName = System.getProperty("os.name").toLowerCase();
               if (osName.contains("win")) {
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
            });
            return null;
         }
      };
   }

   public BrowserFunction getBrowserFunction() { return browserFunction; }

   public static Optional<BrowserFunction> install(final GLSPDiagramEditor editor, final Browser browser) {
      if ((browser.getStyle() & SWT.CHROMIUM) == 0) {
         return Optional.empty();
      }
      ChromiumSelectionFunction function = new ChromiumSelectionFunction(editor, browser);
      browser.execute(INSTALL_FUNCTION);
      return Optional.of(function.getBrowserFunction());
   }
}

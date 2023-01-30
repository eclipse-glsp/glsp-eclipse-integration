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

import java.util.Optional;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;

/**
 * @deprecated Use {@link BrowserKeyBindingForwarderInstaller} instead.
 */
@Deprecated
public final class ChromiumKeyBindingFunction {
   private ChromiumKeyBindingFunction() {}

   public static Optional<BrowserFunction> install(final GLSPDiagramEditor editor, final Browser browser) {
      return new BrowserKeyBindingForwarderInstaller(editor.getSite()).install(browser);
   }
}

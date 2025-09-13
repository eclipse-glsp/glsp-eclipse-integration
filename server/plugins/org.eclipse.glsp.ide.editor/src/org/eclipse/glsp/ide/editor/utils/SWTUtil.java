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
package org.eclipse.glsp.ide.editor.utils;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.widgets.Control;

@SuppressWarnings("deprecation")
public final class SWTUtil {
   private SWTUtil() {}

   public static boolean hasStyle(final Control control, final int style) {
      return control != null && (control.getStyle() & style) != 0;
   }

   public static boolean hasAnyStyle(final Control control, final int... styles) {
      return Arrays.stream(styles).anyMatch(style -> hasStyle(control, style));
   }

   public static boolean hasAllStyles(final Control control, final int... styles) {
      return Arrays.stream(styles).allMatch(style -> hasStyle(control, style));
   }

   public static <C extends Control> Optional<C> ifHasStyle(final C control, final int style) {
      return hasStyle(control, style) ? Optional.of(control) : Optional.empty();
   }

   public static <C extends Control> Optional<C> ifHasAnyStyle(final C control, final int... styles) {
      return hasAnyStyle(control, styles) ? Optional.of(control) : Optional.empty();
   }

   public static <C extends Control> Optional<C> ifHasAllStyles(final C control, final int... styles) {
      return hasAllStyles(control, styles) ? Optional.of(control) : Optional.empty();
   }

   public static boolean isChromium(final Browser browser) {
      return SWTUtil.hasStyle(browser, SWT.CHROMIUM);
   }

   public static boolean isEdge(final Browser browser) {
      return SWTUtil.hasStyle(browser, SWT.EDGE);
   }

   public static boolean isChromiumOrEdge(final Browser browser) {
      return SWTUtil.hasAnyStyle(browser, SWT.CHROMIUM, SWT.EDGE);
   }
}

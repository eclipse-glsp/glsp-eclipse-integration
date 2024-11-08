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
package org.eclipse.glsp.ide.editor.internal.utils;

import java.security.AccessController;
import java.security.PrivilegedAction;

@SuppressWarnings("removal")
public final class SystemUtils {
   private static final String OS_NAME;
   static {
      OS_NAME = AccessController
         .doPrivileged((PrivilegedAction<String>) () -> System.getProperty("os.name").toLowerCase());
   }

   private SystemUtils() {}

   public static boolean isWindows() { return OS_NAME.startsWith("windows"); }

   public static boolean isMacOSX() { return OS_NAME.startsWith("mac os x"); }

   public static boolean isLinux() { return OS_NAME.startsWith("linux"); }
}

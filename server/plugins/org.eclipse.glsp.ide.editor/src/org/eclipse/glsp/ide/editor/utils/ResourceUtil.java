/********************************************************************************
 * Copyright (c) 2020-2022 EclipseSource and others.
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ResourceUtil {
   private static final ResourceUtil INSTANCE = new ResourceUtil();

   public static boolean copyFromResource(final String resourcePath, final File destFile) {
      final ClassLoader classLoader = INSTANCE.getClass().getClassLoader();
      try (InputStream stream = classLoader.getResourceAsStream(resourcePath);
         FileOutputStream fileOutputStream = new FileOutputStream(destFile)) {

         if (stream == null) {
            return false;
         }
         stream.transferTo(fileOutputStream);
      } catch (final IOException e) {
         return false;
      }
      return true;
   }

   private ResourceUtil() {}

}

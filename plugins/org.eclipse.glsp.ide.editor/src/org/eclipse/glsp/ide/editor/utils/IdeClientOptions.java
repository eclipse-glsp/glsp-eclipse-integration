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

import java.io.File;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.glsp.server.utils.ClientOptions;

public final class IdeClientOptions {
   private IdeClientOptions() {}

   public static Optional<IFile> getSourceUriAsIFile(final Map<String, String> options) {
      return ClientOptions.getSourceUriAsFile(options).flatMap(IdeClientOptions::getFileAsIFile);
   }

   public static Optional<IFile> getFileAsIFile(final File file) {
      Path location = new Path(file.getAbsolutePath());
      IFile workspaceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(location);
      return workspaceFile != null && workspaceFile.exists() ? Optional.of(workspaceFile) : Optional.empty();
   }

   public static Optional<File> getUriAsFile(final String uri) {
      return ClientOptions.getSourceUriAsFile(Map.of(ClientOptions.SOURCE_URI, uri));
   }

   public static Optional<IFile> getUriAsIFile(final String uri) {
      return getUriAsFile(uri).flatMap(IdeClientOptions::getFileAsIFile);
   }
}

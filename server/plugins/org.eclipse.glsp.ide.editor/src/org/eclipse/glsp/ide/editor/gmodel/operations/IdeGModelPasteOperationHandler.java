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
package org.eclipse.glsp.ide.editor.gmodel.operations;

import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.command.Command;
import org.eclipse.glsp.ide.editor.clipboard.ClipboardService;
import org.eclipse.glsp.ide.editor.clipboard.ui.JsonTransfer;
import org.eclipse.glsp.server.gmodel.GModelPasteOperationHandler;
import org.eclipse.glsp.server.gson.GraphGsonConfigurationFactory;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.operations.PasteOperation;

import com.google.inject.Inject;

/**
 * An {@link OperationHandler} similar to the GLSP {@link GModelPasteOperationHandler},
 * except that it relies on the local clipboard (instead of relying on Data passed by the client
 * via the operation data).
 */
public class IdeGModelPasteOperationHandler extends GModelPasteOperationHandler {

   @Inject
   protected ClipboardService clipboard;

   @Inject
   public IdeGModelPasteOperationHandler(final GraphGsonConfigurationFactory gsonFactory) {
      super(gsonFactory);
   }

   @Override
   public Optional<Command> createCommand(final PasteOperation operation) {
      operation.setClipboardData(Map.of(
         JsonTransfer.APPLICATION_JSON,
         clipboard.getClipboardContents(JsonTransfer.APPLICATION_JSON).orElse(null)));
      return super.createCommand(operation);
   }
}

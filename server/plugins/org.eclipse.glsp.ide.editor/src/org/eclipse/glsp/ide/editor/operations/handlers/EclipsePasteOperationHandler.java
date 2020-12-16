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
package org.eclipse.glsp.ide.editor.operations.handlers;

import static org.eclipse.glsp.server.utils.GModelUtil.shift;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.glsp.graph.GModelElement;
import org.eclipse.glsp.ide.editor.clipboard.ClipboardService;
import org.eclipse.glsp.ide.editor.clipboard.ui.JsonTransfer;
import org.eclipse.glsp.server.jsonrpc.GraphGsonConfiguratorFactory;
import org.eclipse.glsp.server.model.GModelState;
import org.eclipse.glsp.server.operations.OperationHandler;
import org.eclipse.glsp.server.operations.PasteOperation;
import org.eclipse.glsp.server.operations.gmodel.PasteOperationHandler;

import com.google.inject.Inject;

/**
 * An {@link OperationHandler} similar to the GLSP {@link PasteOperationHandler},
 * except that it relies on the local clipboard (instead of relying on Data passed by the client
 * via the operation data).
 */
public class EclipsePasteOperationHandler extends PasteOperationHandler {

   @Inject
   protected ClipboardService clipboard;

   @Inject
   public EclipsePasteOperationHandler(final GraphGsonConfiguratorFactory gsonFactory) {
      super(gsonFactory);
   }

   @Override
   public void executeOperation(final PasteOperation operation, final GModelState modelState) {

      Optional<String> content = clipboard.getClipboardContents(JsonTransfer.APPLICATION_JSON);

      content.ifPresent(data -> {
         // Use our own clipboard contents
         List<GModelElement> elements = getCopiedElements(data);

         //
         // Copied from super-class implementation
         //
         shift(elements, computeOffset(elements, operation.getEditorContext().getLastMousePosition()));

         Map<String, String> idMap = reassignIds(elements);
         filterElements(elements, idMap);
         rewireEdges(elements, idMap);

         modelState.getRoot().getChildren().addAll(elements);
      });
   }
}

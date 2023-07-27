/********************************************************************************
 * Copyright (c) 2023 EclipseSource and others.
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
package org.eclipse.glsp.ide.workflow.editor;

import org.eclipse.glsp.ide.editor.ui.GLSPDiagramPart;

/**
 * This is an example to open the E4 Part on a file basis.
 * If you want to work with files it is probably better to use the E3 Editor.
 *
 * If you have a use case where you are not working on a file, try the E4 Solution
 */
public class WorkflowPart extends GLSPDiagramPart {

   public WorkflowPart() {
      super("org.eclipse.glsp.workflow.editor");
   }

   @Override
   protected String getInput() { return getPart().getProperties().get("file"); }

}

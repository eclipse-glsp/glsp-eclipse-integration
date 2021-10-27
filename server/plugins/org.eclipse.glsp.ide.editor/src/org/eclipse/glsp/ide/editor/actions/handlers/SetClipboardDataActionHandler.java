/********************************************************************************
 * Copyright (c) 2020-2021 EclipseSource and others.
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
package org.eclipse.glsp.ide.editor.actions.handlers;

import java.util.List;
import java.util.Map;

import org.eclipse.glsp.ide.editor.clipboard.ClipboardService;
import org.eclipse.glsp.server.actions.AbstractActionHandler;
import org.eclipse.glsp.server.actions.Action;
import org.eclipse.glsp.server.features.clipboard.SetClipboardDataAction;

import com.google.inject.Inject;

/**
 * Copy the text from the {@link SetClipboardDataAction} to the Eclipse/SWT Clipboard (System Clipboard).
 */
public class SetClipboardDataActionHandler extends AbstractActionHandler<SetClipboardDataAction> {

   @Inject
   protected ClipboardService clipboard;

   @Override
   protected List<Action> executeAction(final SetClipboardDataAction action) {
      Map<String, String> clipboardData = action.getClipboardData();
      clipboard.setClipboardContents(clipboardData);
      return none();
   }
}

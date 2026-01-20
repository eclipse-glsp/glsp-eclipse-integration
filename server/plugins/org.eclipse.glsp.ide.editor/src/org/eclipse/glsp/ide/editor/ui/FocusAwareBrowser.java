/********************************************************************************
 * Copyright (c) 2020-2024 EclipseSource and others.
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

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;

/**
 * Browser subclassed because of https://bugs.eclipse.org/bugs/show_bug.cgi?id=567629.
 * This is not required on Linux but on Windows.
 * Once this is fixed on Eclipse side, the subclass and the focus listener may be removed.
 */
public class FocusAwareBrowser extends Browser {
   protected final AtomicBoolean focusTracker = new AtomicBoolean(false);

   public FocusAwareBrowser(final Composite parent, final int style) {
      super(parent, style);
      addFocusListener(new FocusListener() {
         @Override
         public void focusLost(final FocusEvent e) {
            focusTracker.set(false);
         }

         @Override
         public void focusGained(final FocusEvent e) {
            focusTracker.set(true);
         }
      });
   }

   @Override
   public boolean isFocusControl() {
      if (focusTracker != null && !focusTracker.get()) {
         return false;
      }
      return super.isFocusControl();
   }

   @Override
   protected void checkSubclass() {}
}

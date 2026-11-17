/********************************************************************************
 * Copyright (c) 2026 EclipseSource and others.
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.eclipse.glsp.ide.editor.ui.BrowserKeyBindingForwarderInstaller.SerializableKeyEvent;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.ui.keys.IBindingService;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

class BrowserKeyBindingForwarderInstallerTest {

   private final BrowserKeyBindingForwarderInstaller installer = new BrowserKeyBindingForwarderInstaller(
      (IBindingService) null);

   @Test
   void mapsModifiersAndKey() throws ParseException {
      assertEquals(Optional.of(KeySequence.getInstance("CTRL+S")), keySequence(true, false, false, "s"));
      assertEquals(Optional.of(KeySequence.getInstance("CTRL+SHIFT+S")), keySequence(true, false, true, "S"));
      assertEquals(Optional.of(KeySequence.getInstance("ALT+F5")), keySequence(false, true, false, "F5"));
   }

   @Test
   void ignoresKeysWithoutEclipseName() {
      assertTrue(keySequence(true, false, false, "Dead").isEmpty());
   }

   Optional<KeySequence> keySequence(final boolean ctrl, final boolean alt, final boolean shift, final String key) {
      String json = String.format("{\"ctrlKey\":%b,\"altKey\":%b,\"shiftKey\":%b,\"key\":\"%s\"}", ctrl, alt, shift,
         key);
      return installer.getKeySequence(new Gson().fromJson(json, SerializableKeyEvent.class));
   }
}

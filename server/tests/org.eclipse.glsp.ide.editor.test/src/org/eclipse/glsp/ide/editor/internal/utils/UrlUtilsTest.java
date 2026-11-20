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
package org.eclipse.glsp.ide.editor.internal.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

class UrlUtilsTest {

   @Test
   void createsUrlWithoutPort() {
      assertEquals("http://localhost/diagram.html", UrlUtils.createUrl("localhost", "diagram.html"));
      assertEquals("https://localhost/diagram.html", UrlUtils.createUrl("localhost", "diagram.html", true));
   }

   @Test
   void createsUrlWithPort() {
      assertEquals("http://localhost:8080/diagram.html", UrlUtils.createUrl("localhost", 8080, "diagram.html"));
   }

   @Test
   void stripsLeadingSlashFromBaseUrl() {
      assertEquals("http://localhost:8080/diagram.html", UrlUtils.createUrl("localhost", 8080, "/diagram.html"));
   }

   @Test
   void appendsEncodedQueryParameters() {
      Map<String, String> params = new LinkedHashMap<>();
      params.put("client", "a b");
      params.put("the uri", "file:/a&b?c");

      assertEquals("http://localhost:8080/diagram.html?client=a+b&the+uri=file%3A%2Fa%26b%3Fc",
         UrlUtils.createUrl("localhost", 8080, "diagram.html", params));
   }
}

/********************************************************************************
 * Copyright (c) 2021-2023 EclipseSource and others.
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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class UrlUtils {
   private static Logger LOG = LogManager.getLogger(UrlUtils.class);

   private UrlUtils() {}

   public static String createUrl(final String host, final String baseUrl) {
      return createUrl(host, baseUrl, false, Collections.emptyMap());
   }

   public static String createUrl(final String host, final String baseUrl, final boolean useHttps) {
      return createUrl(host, baseUrl, useHttps, Collections.emptyMap());
   }

   public static String createUrl(final String host, final String baseUrl,
      final Map<String, String> queryParams) {
      return createUrl(host, baseUrl, false, queryParams);
   }

   public static String createUrl(final String host, final String baseUrl, final boolean useHttps,
      final Map<String, String> queryParams) {
      return createUrl(host, -1, baseUrl, useHttps, queryParams);
   }

   public static String createUrl(final String host, final int port, final String baseUrl) {
      return createUrl(host, port, baseUrl, false);
   }

   public static String createUrl(final String host, final int port, final String baseUrl, final boolean useHttps) {
      return createUrl(host, port, baseUrl, useHttps, Collections.emptyMap());
   }

   public static String createUrl(final String host, final int port, final String baseUrl,
      final Map<String, String> queryParams) {
      return createUrl(host, port, baseUrl, false, queryParams);
   }

   public static String createUrl(final String host, final int port, final String baseUrl, final boolean useHttps,
      final Map<String, String> queryParams) {
      StringBuilder builder = new StringBuilder(useHttps ? "https" : "http")
         .append("://")
         .append(host);
      if (port >= 0) {
         builder.append(":")
            .append(port);
      }
      builder.append("/")
         .append(baseUrl);

      if (!queryParams.isEmpty()) {
         builder.append("?");
         queryParams.entrySet()
            .forEach(e -> builder.append(e.getKey())
               .append("=")
               .append(encodeParameter(e.getValue()))
               .append("&"));
         builder.deleteCharAt(builder.length() - 1);
      }

      return builder.toString();
   }

   private static String encodeParameter(final String parameter) {
      try {
         return URLEncoder.encode(parameter, "UTF-8");
      } catch (UnsupportedEncodingException exception) {
         // UTF-8 is a supported so we should never reach this catch block;
         LOG.warn(exception);
         return parameter;
      }
   }
}

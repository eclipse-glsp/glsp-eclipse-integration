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
package org.eclipse.glsp.ide.editor.ui;

import java.util.Collection;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.glsp.ide.editor.utils.UIUtil;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.BrowserFunction;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.services.IServiceLocator;

import com.google.gson.Gson;

/**
 * Browser function that delegates key events from the browser to the key
 * binding service of the editor. Inspired by
 * https://github.com/maketechnology/chromium.swt/issues/70.
 */
public class BrowserKeyBindingForwarderInstaller implements BrowserFunctionInstaller {
   private static final Logger LOGGER = LogManager.getLogger(BrowserKeyBindingForwarderInstaller.class);

   private static final String FUNCTION_NAME = "$notifyKeybinding";

   // only delegate ctrl/shift/alt events for now to cover most global bindings
   private static final String FUNCTION_CALL = "if(event.ctrlKey || event.shiftKey || event.altKey) { " //
      + FUNCTION_NAME + "(JSON.stringify(" + SerializableKeyEvent.JAVASCRIPT_CONSTRUCTOR_FOR_EVENT + "));" //
      + "}";

   private static final String FUNCTION_INSTALLER = //
      "document.addEventListener('keydown', (event) => { " + FUNCTION_CALL + " });";

   private final IBindingService bindingService;

   public BrowserKeyBindingForwarderInstaller(final IBindingService bindingService) {
      this.bindingService = bindingService;
   }

   public BrowserKeyBindingForwarderInstaller(final IServiceLocator serviceLocator) {
      this.bindingService = serviceLocator.getService(IBindingService.class);
   }

   @Override
   public Optional<BrowserFunction> install(final Browser browser) {
      return doInstall(browser);
   }

   protected Optional<BrowserFunction> doInstall(final Browser browser) {
      BrowserFunction browserFunction = new BrowserFunction(browser, FUNCTION_NAME) {
         @Override
         public Object function(final Object[] arguments) {
            if (arguments.length == 1 && arguments[0] instanceof String) {
               SerializableKeyEvent event = new Gson().fromJson((String) arguments[0], SerializableKeyEvent.class);
               Optional<KeySequence> keySequence = getKeySequence(event);
               if (keySequence.isPresent()) {
                  if (bindingService != null) {
                     forwardKeySequence(bindingService, keySequence.get());
                  }
               }
            }
            return null;
         }
      };
      browser.execute(FUNCTION_INSTALLER);
      return Optional.of(browserFunction);
   }

   protected void forwardKeySequence(final IBindingService bindingService, final KeySequence keySequence) {
      findMatchingBinding(bindingService, keySequence).ifPresent(this::executeCommand);
   }

   @SuppressWarnings("unchecked")
   protected Optional<Binding> findMatchingBinding(final IBindingService bindingService,
      final KeySequence keySequence) {
      Binding perfectMatch = bindingService.getPerfectMatch(keySequence);
      if (perfectMatch == null) {
         Collection<Binding> partialMatches = bindingService.getConflictsFor(keySequence);
         if (partialMatches != null) {
            for (Binding binding : partialMatches) {
               if (binding.getParameterizedCommand().getCommand().isEnabled()) {
                  return Optional.of(binding);
               }
            }
         }
      }
      return Optional.ofNullable(perfectMatch);
   }

   protected void executeCommand(final Binding binding) {
      UIUtil.asyncExec(() -> doExecuteCommand(binding));
   }

   protected void doExecuteCommand(final Binding binding) {
      try {
         if (binding != null && binding.getParameterizedCommand().getCommand().isEnabled()) {
            binding.getParameterizedCommand().executeWithChecks(null, null);
         }
      } catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException exception) {
         LOGGER.error(exception);
      }
   }

   protected Optional<KeySequence> getKeySequence(final SerializableKeyEvent event) {
      String keyString = event.getKey().toUpperCase();
      if (event.isCtrlKey()) {
         keyString = IKeyLookup.CTRL_NAME + KeyStroke.KEY_DELIMITERS + keyString;
      }
      if (event.isAltKey()) {
         keyString = IKeyLookup.ALT_NAME + KeyStroke.KEY_DELIMITERS + keyString;
      }
      if (event.isShiftKey()) {
         keyString = IKeyLookup.SHIFT_NAME + KeyStroke.KEY_DELIMITERS + keyString;
      }
      try {
         KeyStroke stroke = KeyStroke.getInstance(keyString);
         return Optional.ofNullable(KeySequence.getInstance(stroke));
      } catch (IllegalArgumentException | ParseException exception) {
         return Optional.empty();
      }
   }

   protected static class SerializableKeyEvent {
      private static final String JAVASCRIPT_CONSTRUCTOR_FOR_EVENT = "{" //
         + "    altKey: event.altKey, " //
         + "    ctrlKey: event.ctrlKey, " //
         + "    shiftKey: event.shiftKey, " //
         + "    metaKey: event.metaKey, " //
         + "    repeat: event.repeat, " //
         + "    key: event.key, " //
         + "    code: event.code" //
         + " }";

      private boolean altKey;
      private boolean ctrlKey;
      private boolean shiftKey;
      private boolean metaKey;
      private boolean repeat;
      private String code;
      private String key;

      public boolean isAltKey() { return altKey; }

      public boolean isCtrlKey() { return ctrlKey; }

      public boolean isShiftKey() { return shiftKey; }

      public boolean isMetaKey() { return metaKey; }

      public boolean isRepeat() { return repeat; }

      public String getCode() { return code; }

      public String getKey() { return key; }

      @Override
      public String toString() {
         StringBuilder builder = new StringBuilder();
         builder.append("SerializableEvent [isAltKey()=").append(isAltKey()).append(", isCtrlKey()=")
            .append(isCtrlKey()).append(", isShiftKey()=").append(isShiftKey()).append(", isMetaKey()=")
            .append(isMetaKey()).append(", isRepeat()=").append(isRepeat()).append(", getCode()=")
            .append(getCode()).append(", getKey()=").append(getKey()).append("]");
         return builder.toString();
      }

   }
}

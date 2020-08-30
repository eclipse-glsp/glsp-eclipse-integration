package org.eclipse.glsp.integration.editor;

import java.util.Optional;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.glsp.api.action.ActionDispatcher;
import org.eclipse.glsp.api.action.kind.ExportSVGAction;
import org.eclipse.glsp.api.protocol.GLSPServerException;
import org.eclipse.glsp.integration.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.integration.editor.utils.UIUtil;

import com.google.inject.Injector;

public class ExportDiagramHandler extends AbstractHandler {

   @Override
   public Object execute(final ExecutionEvent event) throws ExecutionException {

      UIUtil.getActiveEditor(GLSPDiagramEditorPart.class).ifPresent(editorPart -> {
         String clientId = (editorPart).getClientId();
         ActionDispatcher actionDispatcher = getInjector(editorPart).getInstance(ActionDispatcher.class);

         actionDispatcher.dispatch(clientId, new ExportSVGAction());

      });
      return null;
   }

   protected Injector getInjector(final GLSPDiagramEditorPart editorPart) {
      Optional<Injector> injector = GLSPEditorIntegrationPlugin.getDefault().getServerProviderRegistry()
         .getInjector(editorPart);
      if (!injector.isPresent()) {
         throw new GLSPServerException(
            "Could not retrieve GLSP injector. GLSP editor is not properly configured: "
               + editorPart.getEditorSite().getId());
      }
      return injector.get();
   }

}

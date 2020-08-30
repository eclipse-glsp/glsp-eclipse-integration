package org.eclipse.glsp.integration.editor;

import javax.websocket.Session;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.glsp.integration.editor.ui.GLSPEditorIntegrationPlugin;
import org.eclipse.glsp.server.websocket.GLSPServerEndpoint;
import org.eclipse.ui.statushandlers.StatusManager;

public class DiagramWebsocketEndpoint extends GLSPServerEndpoint {

	@Override
	public void onError(Session session, Throwable throwable) {
		StatusManager.getManager().handle(
				new Status(IStatus.ERROR, GLSPEditorIntegrationPlugin.PLUGIN_ID, "Error in diagram web socket", throwable));
		super.onError(session, throwable);
	}

}

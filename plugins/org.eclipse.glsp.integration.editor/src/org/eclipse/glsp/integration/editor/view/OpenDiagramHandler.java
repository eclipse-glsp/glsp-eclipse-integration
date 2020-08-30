package org.eclipse.glsp.integration.editor.view;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.handlers.HandlerUtil;

public class OpenDiagramHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
//		if (selection)
		return null;
	}

//	protected void getFile(ISelection selection) {
//		if(selection instanceof ITextSelection) {
//			val editorInput = PlatformUI.workbench.activeWorkbenchWindow?.activePage?.activeEditor?.editorInput
//			if(editorInput instanceof IFileEditorInput)
//				return editorInput.file
//		}
//		if (selection instanceof IStructuredSelection) 
//			selection.toList.filter(IFile).head
//		
//	}
//
}

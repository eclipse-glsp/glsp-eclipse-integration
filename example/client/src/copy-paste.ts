import {
    Action,
    configureActionHandler,
    EditorContextService,
    GLSP_TYPES,
    IActionDispatcher,
    IActionHandler,
    IAsyncClipboardService,
    TYPES,
    ViewerOptions
} from "@eclipse-glsp/client";
import {
    CutOperation,
    PasteOperation,
    RequestClipboardDataAction
} from "@eclipse-glsp/client/lib/features/copy-paste/copy-paste-actions";
import { ContainerModule, inject, injectable } from "inversify";

// Eclipse-specific integration: in Eclipse, we trigger the Copy/Paste actions from
// the IDE Keybindings. We don't use the browser events. This is fine, because we
// don't need browser clipboard support (We use the Eclipse System Clipboard); so 
// we don't need special permission from the Browser.

@injectable()
export class EclipseCopyPasteActionHandler implements IActionHandler {
    @inject(TYPES.IActionDispatcher) protected actionDispatcher: IActionDispatcher;
    @inject(TYPES.ViewerOptions) protected viewerOptions: ViewerOptions;
    @inject(GLSP_TYPES.IAsyncClipboardService) protected clipboadService: IAsyncClipboardService;
    @inject(EditorContextService) protected editorContext: EditorContextService;

    handle(action: Action) {
        switch (action.kind) {
            case "invoke-copy":
                this.handleCopy();
                break;
            case "invoke-paste":
                this.handlePaste();
                break;
            case "invoke-cut":
                this.handleCut();
                break;
        }
    }

    handleCopy() {
        if (this.shouldCopy()) {
            this.actionDispatcher
                .request(RequestClipboardDataAction.create(this.editorContext.get()));
        } else {
            this.clipboadService.clear();
        }
    }

    handleCut(): void {
        if (this.shouldCopy()) {
            this.handleCopy();
            this.actionDispatcher.dispatch(new CutOperation(this.editorContext.get()));
        }
    }

    handlePaste(): void {
        // In the Eclipse Integration case, the server manages its own clipboard.
        // Just pass an empty clipboard data to remain compliant with the API.
        const clipboardData = {};
        this.actionDispatcher.dispatch(new PasteOperation(clipboardData, this.editorContext.get()));
    }

    protected shouldCopy() {
        return this.editorContext.get().selectedElementIds.length > 0 && document.activeElement instanceof SVGElement
            && document.activeElement.parentElement && document.activeElement.parentElement.id === this.viewerOptions.baseDiv;
    }
}

export const eclipseCopyPasteModule = new ContainerModule((bind, _unbind, isBound) => {
    configureActionHandler({ bind, isBound }, "invoke-copy", EclipseCopyPasteActionHandler);
    configureActionHandler({ bind, isBound }, "invoke-cut", EclipseCopyPasteActionHandler);
    configureActionHandler({ bind, isBound }, "invoke-paste", EclipseCopyPasteActionHandler);
});

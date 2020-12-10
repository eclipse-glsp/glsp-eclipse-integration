import {
    Action,
    configureActionHandler,
    DeleteElementOperation,
    EditorContextService,
    IActionDispatcher,
    IActionHandler,
    TYPES
} from "@eclipse-glsp/client";
import { ContainerModule, inject, injectable } from "inversify";

export class InvokeDeleteAction implements Action {
    static KIND = 'invoke-delete';
    readonly kind = InvokeDeleteAction.KIND;
    constructor() { }
}

export function isInvokeDeleteAction(action: Action): action is InvokeDeleteAction {
    return action.kind === InvokeDeleteAction.KIND;
}

@injectable()
export class InvokeDeleteActionHandler implements IActionHandler {
    @inject(TYPES.IActionDispatcher) protected actionDispatcher: IActionDispatcher;
    @inject(EditorContextService) protected editorContext: EditorContextService;

    handle(action: Action) {
        if (isInvokeDeleteAction(action)) {
            this.handleDelete();
        }
    }

    handleDelete() {
        this.actionDispatcher.dispatch(new DeleteElementOperation(this.editorContext.get().selectedElementIds));
    }
}

export const eclipseDeleteModule = new ContainerModule((bind, _unbind, isBound) => {
    configureActionHandler({ bind, isBound }, "invoke-delete", InvokeDeleteActionHandler);
});

package brig.concord.meta.model;

import java.util.List;

public class StepElementMetaType extends IdentityElementMetaType {

    private static final List<IdentityMetaType> steps = List.of(
            TaskStepMetaType.getInstance(),
            CallStepMetaType.getInstance(),
            LogStepMetaType.getInstance(),
            IfStepMetaType.getInstance(),
            ReturnStepMetaType.getInstance(),
            ExitStepMetaType.getInstance(),
            CheckpointStepMetaType.getInstance(),
            SetStepMetaType.getInstance(),
            ThrowStepMetaType.getInstance(),
            SuspendStepMetaType.getInstance(),
            ExprStepMetaType.getInstance(),
            ParallelStepMetaType.getInstance(),
            ScriptStepMetaType.getInstance(),
            SwitchStepMetaType.getInstance(),
            TryStepMetaType.getInstance(),
            BlockStepMetaType.getInstance(),
            FormStepMetaType.getInstance()
    );

    private static final StepElementMetaType INSTANCE = new StepElementMetaType();

    public static StepElementMetaType getInstance() {
        return INSTANCE;
    }

    protected StepElementMetaType() {
        super("Steps", List.copyOf(steps));
    }
}

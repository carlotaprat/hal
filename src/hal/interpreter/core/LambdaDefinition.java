package hal.interpreter.core;

import hal.interpreter.types.HalModule;


public class LambdaDefinition extends MethodDefinition
{
    private ReferenceRecord context;

    public LambdaDefinition(HalModule module, ReferenceRecord context, Params.Param...params) {
        this.module = module;
        this.name = "yield";
        this.params = new Params(params);
        this.context = context;
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord(context);
    }
}

package hal.interpreter.core;

import hal.interpreter.types.HalModule;
import hal.interpreter.types.HalObject;


public class LambdaDefinition extends MethodDefinition
{
    private ReferenceRecord context;

    public LambdaDefinition(HalModule module, ReferenceRecord context, Params.Param...params) {
        this.module = module;
        this.klass = null;
        this.name = "yield";
        this.params = new Params(params);
        this.context = context;
    }

    public LambdaDefinition(MethodDefinition def) {
        module = null;
        klass = null;
        name = "yield";
        params = def.params;
        context = null;
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord(context);
    }

    public HalObject getInstance() {
        return context.getVariable("self");
    }
}

package hal.interpreter.core;

import hal.interpreter.HalTree;
import hal.interpreter.types.HalModule;
import hal.interpreter.types.HalObject;


public class LambdaDefinition extends MethodDefinition
{
    public ReferenceRecord context;

    public LambdaDefinition(HalModule mod, HalTree t, ReferenceRecord contxt) {
        module = mod;
        name = "yield";
        tree = t;
        params = new Params(t.getChild(0));
        block = t.getChild(1);
        context = contxt;
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord("yield", context);
    }

    public HalObject getInstance() {
        return context.getVariable("self");
    }
}

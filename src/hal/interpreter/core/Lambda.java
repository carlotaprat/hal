package hal.interpreter.core;

import hal.interpreter.HalTree;
import hal.interpreter.types.HalObject;


public class Lambda extends MethodDefinition
{
    public ReferenceRecord context;
    public HalObject instance;

    public Lambda(HalTree t, ReferenceRecord contxt) {
        name = "yield";
        tree = t;
        params = t.getChild(0);
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

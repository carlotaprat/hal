package hal.interpreter.core;

import hal.interpreter.HalTree;
import hal.interpreter.types.HalModule;


public class MethodDefinition
{
    public HalModule module;
    public String name;
    public HalTree tree;
    public HalTree params;
    public HalTree block;

    public MethodDefinition() { }

    public MethodDefinition(HalModule mod, HalTree t) {
        module = mod;
        name = t.getChild(0).getText();
        tree = t;
        params = t.getChild(1);
        block = t.getChild(2);
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord(name, null);
    }
}

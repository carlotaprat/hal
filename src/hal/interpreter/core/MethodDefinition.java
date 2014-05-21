package hal.interpreter.core;

import hal.interpreter.types.HalModule;
import hal.interpreter.types.HalObject;


public class MethodDefinition
{
    public HalModule module;
    public HalObject klass;
    public String name;
    public Params params;

    public MethodDefinition() { }

    public MethodDefinition(HalModule module, HalObject klass, String name, Params.Param...params) {
        this.module = module;
        this.klass = klass;
        this.name = name;
        this.params = new Params(params);
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord(null);
    }
}

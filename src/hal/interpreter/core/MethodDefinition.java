package hal.interpreter.core;

import hal.interpreter.types.HalModule;


public class MethodDefinition
{
    public HalModule module;
    public String name;
    public Params params;

    public MethodDefinition() { }

    public MethodDefinition(HalModule module, String name, Params.Param...params) {
        this.module = module;
        this.name = name;
        this.params = new Params(params);
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord(null);
    }
}

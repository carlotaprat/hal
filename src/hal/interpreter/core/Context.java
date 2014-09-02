package hal.interpreter.core;


import hal.interpreter.types.HalModule;

public class Context {
    public HalModule module;
    public ReferenceRecord record;
    public boolean isMethod;

    public Context(HalModule module, ReferenceRecord record, boolean isMethod) {
        this.module = module;
        this.record = record;
        this.isMethod = isMethod;
    }
}

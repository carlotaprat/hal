package hal.interpreter.core;


import hal.interpreter.types.HalModule;

public class Context {
    public HalModule module;
    public ReferenceRecord record;

    public Context(HalModule module, ReferenceRecord record) {
        this.module = module;
        this.record = record;
    }
}

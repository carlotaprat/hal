package hal.interpreter.types;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;


public class HalModule extends HalObject<String>
{
    public HalModule(String name) {
        super(name);
    }

    public HalBoolean bool() { return new HalBoolean(true); }
    public HalString str() { return new HalString("<Module: " + value + ">"); }
    public ReferenceRecord getInstanceRecord() { return HalKernel.klass.getInstanceRecord(); }
    public HalClass getKlass() { return HalModule.klass; }

    public static final HalClass klass = new HalClass("Module", HalKernel.klass);
}

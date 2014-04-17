package hal.interpreter.types;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;


public class HalModule extends HalPackage
{
    public HalModule(String name, HalPackage parent) {
        super(name, parent);
    }

    public HalString str() { return new HalString("<Module: " + value + ">"); }
    public ReferenceRecord getInstanceRecord() { return getRecord(); }
    public HalClass getKlass() { return HalModule.klass; }

    public static final HalClass klass = new HalClass("Module", HalKernel.klass);
}

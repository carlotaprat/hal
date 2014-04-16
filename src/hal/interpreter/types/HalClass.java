package hal.interpreter.types;


import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;

public abstract class HalClass extends HalObject<String>
{
    public static final HalClass klass = new HalClass("Class") {
        public void initRecord() {}
        public ReferenceRecord getInstanceRecord() { return HalClass.record; }
    };
    public static final ReferenceRecord record = new ReferenceRecord(klass.value, null);

    public HalClass(String name) {
        value = name;
    }

    public HalString str() {
        return new HalString("<Class: " + value + ">");
    }

    public HalBoolean bool() { return new HalBoolean(true); }

    public void solveDependency() {
        super.initRecord();
        HalClass.record.parent = HalObject.record;
    }

    public abstract ReferenceRecord getInstanceRecord();
    public HalClass getKlass(){ return HalClass.klass; }
}

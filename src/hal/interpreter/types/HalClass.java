package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.types.enumerable.HalString;

public abstract class HalClass extends HalKernel<String>
{
    public static final HalClass klass = new HalClass("Class") {
        public ReferenceRecord getInstanceRecord() { return HalClass.record; }
    };

    public HalClass(String name) {
        value = name;
    }

    public HalString str() {
        return new HalString("<class: " + value + ">");
    }

    public HalBoolean bool() {
        return new HalBoolean(true);
    }

    public HalObject _new(HalObject... args) { throw new NameException("new"); }
    public abstract ReferenceRecord getInstanceRecord();


    private static final Reference __new__ = new Reference(new BuiltinMethod("new") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return ((HalClass) instance)._new(args);
        }
    });

    public static final ReferenceRecord record = new ReferenceRecord(klass.value, HalKernel.record,
            __new__
    );
    public ReferenceRecord getRecord(){ return record; }
    public HalClass getKlass(){ return klass; }
}

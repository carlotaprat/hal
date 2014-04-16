package hal.interpreter.types.enumerable;


import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;

public class HalString extends HalEnumerable<String>
{
    public static final HalClass klass = new HalClass("String") {
        public ReferenceRecord getInstanceRecord() { return HalString.record; }
    };
    public HalClass getKlass() { return klass; }

    public HalString(String s) {
        value = s;
    }

    public String toString(){
        return value;
    }

    public HalString repr() {
        return new HalString("'" + value + "'");
    }

    public HalString str() {
        return new HalString(value);
    }

    public HalString getitem(HalObject index) {
        HalInteger i = (HalInteger) index;

        return new HalString(value.substring(i.value, i.value+1));
    }

    public HalInteger size() {
        return new HalInteger(value.length());
    }

    public static final ReferenceRecord record = new ReferenceRecord(klass.value, HalEnumerable.record);
}

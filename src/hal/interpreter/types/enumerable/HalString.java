package hal.interpreter.types.enumerable;


import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.numeric.HalInteger;

public class HalString extends HalEnumerable<String>
{
    public static final String classId = "String";

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

    public HalString getitem(HalInteger index) {
        return new HalString(value.substring(index.value, index.value+1));
    }

    public HalInteger size() {
        return new HalInteger(value.length());
    }

    public static final ReferenceRecord record = new ReferenceRecord(classId, HalEnumerable.record);
    public ReferenceRecord getRecord() {
        return record;
    }
}

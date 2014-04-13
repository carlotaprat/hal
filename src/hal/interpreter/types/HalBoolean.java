package hal.interpreter.types;


import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;

public class HalBoolean extends HalObject<Boolean>
{
    private static final String classId = "Boolean";

    public HalBoolean(Boolean b)
    {
        super(b);
    }

    public Boolean toBoolean() {
        return value;
    }
    public HalString str() { return new HalString(value ? "true" : "false"); }
    public HalBoolean bool() { return new HalBoolean(value); }

    private static final ReferenceRecord record = new ReferenceRecord(classId, HalObject.record);
    public ReferenceRecord getRecord() {
        return record;
    }
}

package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.HalString;

public class HalInteger extends HalNumber
{
    private static final String classId = "Integer";

    public HalInteger(Integer i)
    {
        super(i);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record);
    
    public ReferenceRecord getRecord() {
        return record;
    }
}

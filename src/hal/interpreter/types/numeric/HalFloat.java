package hal.interpreter.types.numeric;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalObject;

public class HalFloat extends HalNumber {
    
    public HalFloat(float f) {
        super(f);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record);
    
    public ReferenceRecord getRecord() {
        return record;
    }
    
}

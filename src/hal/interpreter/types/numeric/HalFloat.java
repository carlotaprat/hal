package hal.interpreter.types.numeric;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;

public class HalFloat extends HalNumber<Double>
{
    public HalFloat(float f) {
        super((double) f);
    }
    
    public HalFloat(double d) {
        super(d);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record);
    
    public ReferenceRecord getRecord() {
        return record;
    }

    public HalBoolean bool() {
        return new HalBoolean(toFloat() != 0.0);
    }

    @Override
    public HalNumber neg() {
        return new HalFloat(-toFloat());
    }

    @Override
    public HalNumber add(HalNumber n) {
        return new HalFloat(toFloat() + n.toFloat());
    }
    
}

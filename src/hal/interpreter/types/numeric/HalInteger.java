package hal.interpreter.types.numeric;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalBoolean;

public class HalInteger extends HalNumber<Integer>
{
    private static final String classId = "Integer";

    public HalInteger(Integer i)
    {
        super(i);
    }
    
    public HalInteger(float f) {
        super((int) f);
    }

    public HalBoolean bool() {
        return new HalBoolean(value != 0);
    }

    public HalInteger(double d) {
        super((int) d);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record);
    
    public ReferenceRecord getRecord() {
        return record;
    }

    @Override
    public boolean isZero() {
        return value == 0;
    }
    
    @Override
    public HalNumber neg() {
        return new HalInteger(-value);
    }
    
    @Override
    public HalNumber add(HalNumber n) {
        // TODO: Pensar en alguna cosa per tal que la conversió la faci java
        // alguna cosa com getValue per evitar de fer l'if
        if (n instanceof HalFloat)
            return new HalFloat(toInteger() + n.toFloat());

        return new HalInteger(toInteger() + n.toInteger());
        
    }

    @Override
    public HalNumber sub(HalNumber n) {
        if (n instanceof HalFloat)
            return new HalFloat(toInteger() - n.toFloat());

        return new HalInteger(toInteger() - n.toInteger());
    }

    @Override
    public HalNumber mul(HalNumber n) {
        if (n instanceof HalFloat)
            return new HalFloat(toInteger() * n.toFloat());

        return new HalInteger(toInteger() * n.toInteger());
    }

    @Override
    public HalNumber div(HalNumber n) {
        if (n instanceof HalFloat)
            return new HalFloat(toInteger() / n.toFloat());

        return new HalInteger(toInteger() / n.toInteger());
    }

    @Override
    public HalNumber mod(HalNumber n) {
        return new HalInteger(toInteger() % ((HalInteger)n).toInteger());
    }

}
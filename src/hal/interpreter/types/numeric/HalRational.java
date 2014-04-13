package hal.interpreter.types.numeric;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalBoolean;


public class HalRational extends HalNumber<Rational> {
    
    private static final String classId = "Rational";
    
    public HalRational(Integer i) {
        super(new Rational(i));
    }
    
    public HalRational(Rational r) {
        super(r);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record);

    public ReferenceRecord getRecord() {
        return record;
    }
    
    private static HalNumber RorI(Rational r) {
        if (r.isInt())
            return new HalInteger(r.getNum());
        return new HalRational(r);
    }
    
    private static Rational toR(HalNumber n) {
        if (n instanceof HalInteger)
            return new Rational(n.toInteger());
        return ((HalRational)n).value;
    }
        
    @Override
    public boolean isZero() {
        return value.getNum() == 0;
    }

    @Override
    public HalNumber neg() {
        return new HalRational(new Rational(-value.getNum(), value.getDen()));
    }

    @Override
    public HalNumber add(HalNumber n) {
        Rational r = value.add(toR(n));
        return RorI(r);
    }

    @Override
    public HalNumber sub(HalNumber n) {
        Rational r = value.sub(toR(n));
        return RorI(r);
    }

    @Override
    public HalNumber mul(HalNumber n) {
        Rational r = value.mul(toR(n));
        return RorI(r);
    }

    @Override
    public HalNumber div(HalNumber n) {
        Rational r = value.div(toR(n));
        return RorI(r);
    }

    @Override
    public HalBoolean eq(HalNumber n) {
        Rational r = toR(n);
        return new HalBoolean(value.getNum() == r.getNum() && value.getDen() == r.getDen());
    }

    @Override
    public HalBoolean lt(HalNumber n) {
        return new HalBoolean(toFloat() < n.toFloat());
    }
    
}

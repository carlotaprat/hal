package hal.interpreter.types.numeric;

import hal.interpreter.core.data.Rational;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;


public class HalRational extends HalNumber<Rational>
{
    public static final HalClass klass = new HalClass("Rational") {
        public ReferenceRecord getInstanceRecord() { return HalRational.record; }
    };

    public HalRational(Integer i) {
        super(new Rational(i));
    }
    
    public HalRational(Rational r) {
        super(r);
    }
    
    private static final ReferenceRecord record = new ReferenceRecord(klass.value, HalNumber.record);
    public HalClass getKlass() { return klass; }
    
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

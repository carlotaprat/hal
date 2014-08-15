package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.data.Rational;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalObject;


public class HalRational extends HalNumber<Rational>
{

    public HalRational(Integer i) {
        super(new Rational(i));
    }

    public HalRational(Rational r) {
        super(r);
    }

    public HalClass getKlass() { return HalRational.klass; }

    public static HalNumber RorI(Rational r) {
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
    public Object toFormat(){
        return toFloat();
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
        return RorI(value.add(((HalRational) n).value));
    }

    @Override
    public HalNumber sub(HalNumber n) {
        return RorI(value.sub(((HalRational) n).value));
    }

    @Override
    public HalNumber mul(HalNumber n) {
        return RorI(value.mul(((HalRational) n).value));
    }

    @Override
    public HalNumber pow(HalNumber n) {
        Rational r = ((HalRational) n).value;
        if (r.isInt()) {
            return new HalRational(new Rational(
                    HalInteger.power(value.getNum(), r.intValue()).toInteger(),
                    HalInteger.power(value.getDen(), r.intValue()).toInteger()
            ));
        }

        return new HalFloat(Math.pow(toFloat(), n.toFloat()));
    }

    @Override
    public HalNumber div(HalNumber n) {
        return RorI(value.div(((HalRational) n).value));
    }

    @Override
    public boolean canCoerce(HalObject n) {
        return n instanceof HalRational || n instanceof HalInteger;
    }

    @Override
    public HalNumber coerce(HalObject n) {
        if (n instanceof HalInteger)
            return new HalRational(((HalInteger) n).toInteger());
        return (HalRational) n;
    }

    @Override
    public HalBoolean eq(HalNumber n) {
        if (canCoerce(n)) {
            Rational r = toR(n);
            return new HalBoolean(value.getNum() == r.getNum() && value.getDen() == r.getDen());
        }
        else
            return new HalBoolean(value.doubleValue() == n.toFloat());
    }

    @Override
    public HalBoolean lt(HalNumber n) {
        return new HalBoolean(toFloat() < n.toFloat());
    }

    private static final Reference den = new Reference(new Builtin("den") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalInteger(((Rational)instance.value).getDen());
        }
    });

    private static final Reference num = new Reference(new Builtin("num") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalInteger(((Rational)instance.value).getNum());
        }
    });

    public static final HalClass klass = new HalClass("Rational", HalNumber.klass,
            den,
            num
    );

}

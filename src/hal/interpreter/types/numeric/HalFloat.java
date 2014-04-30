package hal.interpreter.types.numeric;

import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalObject;


public class HalFloat extends HalNumber<Double>
{
    public static final HalClass klass = new HalClass("Float", HalNumber.klass);

    public HalFloat(float f) {
        super((double) f);
    }

    public HalFloat(double d) {
        super(d);
    }

    public HalClass getKlass() {
        return HalFloat.klass;
    }

    @Override
    public boolean isZero() {
        return toFloat() == 0.0;
    }

    @Override
    public HalNumber neg() {
        return new HalFloat(-toFloat());
    }

    @Override
    public HalNumber add(HalNumber n) {
        return new HalFloat(toFloat() + n.toFloat());
    }

    @Override
    public HalNumber sub(HalNumber n) {
        return new HalFloat(toFloat() - n.toFloat());
    }

    @Override
    public HalNumber mul(HalNumber n) {
        return new HalFloat(toFloat() * n.toFloat());
    }

    @Override
    public HalNumber pow(HalNumber n) {
        return new HalFloat(Math.pow(toFloat(), n.toFloat()));
    }

    @Override
    public HalNumber div(HalNumber n) {
        return new HalFloat(toFloat() / n.toFloat());
    }

    @Override
    public HalBoolean eq(HalNumber n) {
        return new HalBoolean(toFloat() == n.toFloat());
    }

    @Override
    public HalBoolean lt(HalNumber n) {
        return new HalBoolean(toFloat() < n.toFloat());
    }

    @Override
    public boolean canCoerce(HalObject n) {
        return n instanceof HalNumber;
    }

    @Override
    public HalNumber coerce(HalObject n) {
        return new HalFloat(((HalNumber) n).toFloat());
    }
}

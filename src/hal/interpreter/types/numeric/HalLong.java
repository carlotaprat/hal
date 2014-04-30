package hal.interpreter.types.numeric;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalString;
import java.math.BigInteger;

public class HalLong extends HalNumber<BigInteger> {

    public static final HalClass klass = new HalClass("Integer", HalNumber.klass);

    public HalLong(Integer i) {
        super(BigInteger.valueOf(i));
    }

    public HalLong(String s) {
        super(new BigInteger(s));
    }

    public HalLong(BigInteger bi) {
        super(bi);
    }

    public static HalNumber LorI(BigInteger bi) {
        if (bi.bitLength() < 32)
            return new HalInteger(bi.intValue());
        return new HalLong(bi);
    }

    @Override
    public String toString() { return value.toString(); }

    @Override
    public HalString str() { return new HalString(value.toString() + "L"); }

    @Override
    public HalClass getKlass() {
        return HalInteger.klass;
    }

    @Override
    public boolean isZero() {
        return value == BigInteger.ZERO;
    }

    @Override
    public HalNumber neg() {
        return new HalLong(value.negate());
    }

    @Override
    public HalNumber add(HalNumber n) {
        return new HalLong(value.add(((HalLong) n).value));
    }

    @Override
    public HalNumber sub(HalNumber n) {
        return LorI(value.subtract(((HalLong) n).value));
    }

    @Override
    public HalNumber mul(HalNumber n) {
        return new HalLong(value.multiply(((HalLong) n).value));
    }

    @Override
    public HalNumber div(HalNumber n) {
        return LorI(value.divide(((HalLong) n).value));
    }

    @Override
    public HalNumber mod(HalNumber n) {
        return LorI(value.mod(((HalLong) n).value));
    }

    @Override
    public HalNumber pow(HalNumber n) {
        return new HalLong(value.pow(n.toInteger()));
    }

    @Override
    public HalBoolean eq(HalNumber n) {
        BigInteger bi = new BigInteger(n.toString());
        return new HalBoolean(value.equals(bi));
    }

    @Override
    public HalBoolean lt(HalNumber n) {
        BigInteger bi = new BigInteger(n.toString());
        return new HalBoolean(value.compareTo(bi) < 0);
    }

    @Override
    public boolean canCoerce(HalObject n) {
        return n instanceof HalInteger || n instanceof HalLong;
    }

    @Override
    public HalNumber coerce(HalObject n) {
        return new HalLong(n.toString());
    }
}

package hal.interpreter.types.numeric;

import hal.interpreter.core.data.Rational;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalObject;
import java.math.BigInteger;

public class HalInteger extends HalNumber<Integer>
{
    public static final HalClass klass = new HalClass("Integer", HalNumber.klass);

    public HalInteger(Integer i) {
        super(i);
    }

    public HalInteger(Float f) {
        super(f.intValue());
    }

    public HalInteger(double d) {
        super((int) d);
    }

    public HalClass getKlass() { return HalInteger.klass; }

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
        if (addOverflows(toInteger(), n.toInteger()))
            return new HalLong(toInteger()).add(new HalLong(n.toInteger()));
        return new HalInteger(toInteger() + n.toInteger());
    }

    @Override
    public HalNumber sub(HalNumber n) {
        if (subOverflows(toInteger(), n.toInteger()))
            return new HalLong(toInteger()).sub(new HalLong(n.toInteger()));
        return new HalInteger(toInteger() - n.toInteger());
    }

    @Override
    public HalNumber mul(HalNumber n) {
        if (mulOverflows(toInteger(), n.toInteger()))
            return new HalLong(toInteger()).mul(new HalLong(n.toInteger()));
        return new HalInteger(toInteger() * n.toInteger());
    }

    @Override
    public HalNumber pow(HalNumber n) {
        int exp = ((HalInteger) n).value;
        return power(value, exp);
    }

    public static HalNumber power(int a, int n) {
        // Special cases
        if(a == 1) return new HalInteger(1);
        if(a == -1) return (n%2 == 0)? new HalInteger(1): new HalInteger(-1);
        if(a == 0 && n == 0) throw new InvalidArgumentsException();

        // Base cases
        if (n == 0) return new HalInteger(1);
        if (n == 1) return new HalInteger(a);

        int result = 1;
        boolean overflow = false;
        while (n > 0 && !overflow) {
            if (n % 2 == 1) {
                overflow |= mulOverflows(result, a);
                if (!overflow) {
                    result *= a;
                    n -= 1;
                }
            }
            overflow |= mulOverflows(a, a);
            if (!overflow) {
                a *= a;
                n /= 2;
            }
        }
        if (overflow)
            return HalLong.LorI(BigInteger.valueOf(a).pow(n).multiply(BigInteger.valueOf(result)));

        return new HalInteger(result);
    }

    @Override
    public HalNumber div(HalNumber n) {
        return HalRational.RorI(new Rational(toInteger(), n.toInteger()));
    }

    @Override
    public HalNumber mod(HalNumber n) {
        int r = toInteger() % n.toInteger();
        // Positive modulus
        return new HalInteger((r < 0)? r + n.toInteger(): r);
    }

    @Override
    public HalNumber ddiv(HalNumber n) {
        return new HalInteger(toInteger() / n.toInteger());
    }

    @Override
    public HalBoolean eq(HalNumber n) {
        return ((HalBoolean)(new HalRational(toInteger())).methodcall("__eq__", n));
    }

    @Override
    public HalBoolean lt(HalNumber n) {
        return ((HalBoolean)(new HalRational(toInteger())).methodcall("__lt__", n));
    }

    @Override
    public boolean canCoerce(HalObject n) {
        return n instanceof HalInteger;
    }

    @Override
    public HalNumber coerce(HalObject n) {
        return ((HalInteger) n);
    }

    private static boolean addOverflows(int left, int right) {
        if (right < 0 && right != Integer.MIN_VALUE) {
            return subOverflows(left, -right);
        } else {
            return (~(left ^ right) & (left ^ (left + right))) < 0;
        }
    }

    private static boolean subOverflows(int left, int right) {
        if (right < 0) {
            return addOverflows(left, -right);
        } else {
            return ((left ^ right) & (left ^ (left - right))) < 0;
        }
    }

    private static boolean mulOverflows(int left, int right) {
        return Integer.numberOfLeadingZeros(Math.abs(left)) +
               Integer.numberOfLeadingZeros(Math.abs(right)) < 32 + (left >> 31 ^ right >> 31);
    }
}

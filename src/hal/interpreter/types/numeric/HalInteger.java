package hal.interpreter.types.numeric;

import hal.interpreter.core.data.Rational;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;

public class HalInteger extends HalNumber<Integer>
{
    public static final HalClass klass = new HalClass("Integer", HalNumber.klass);

    public HalInteger(Integer i) {
        super(i);
    }

    public HalInteger(float f) {
        super((int) f);
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
        // TODO: Pensar en alguna cosa per tal que la conversió la faci java
        // alguna cosa com getValue per evitar de fer l'if
        if (n instanceof HalFloat) {
            return new HalFloat(toInteger() + n.toFloat());
        }

        if (n instanceof HalRational) {
            return new HalRational((new Rational(toInteger())).add(((HalRational) n).value));
        }

        return new HalInteger(toInteger() + n.toInteger());

    }

    @Override
    public HalNumber sub(HalNumber n) {
        if (n instanceof HalFloat) {
            return new HalFloat(toInteger() - n.toFloat());
        }

        if (n instanceof HalRational) {
            return new HalRational((new Rational(toInteger())).sub(((HalRational) n).value));
        }

        return new HalInteger(toInteger() - n.toInteger());
    }

    @Override
    public HalNumber mul(HalNumber n) {
        if (n instanceof HalFloat) {
            return new HalFloat(toInteger() * n.toFloat());
        }

        if (n instanceof HalRational) {
            return new HalRational((new Rational(toInteger())).mul(((HalRational) n).value));
        }

        return new HalInteger(toInteger() * n.toInteger());
    }

    @Override
    public HalNumber pow(HalNumber n) {
        if(n instanceof HalInteger)
            return new HalInteger(power(value, ((HalInteger) n).value));

        return new HalFloat(Math.pow(toFloat(), n.toFloat()));
    }

    @Override
    public HalNumber div(HalNumber n) {
        if (n instanceof HalFloat) {
            return new HalFloat(toInteger() / n.toFloat());
        }

        return HalRational.RorI(new Rational(toInteger(), n.toInteger()));
    }

    @Override
    public HalNumber mod(HalNumber n) {
        return new HalInteger(toInteger() % n.toInteger());
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

    public static int power(int a, int n) {
        if(n == 0) return 1;
        if(n == 1) return a;
        if(n % 2 == 0) return power(a*a, n/2);
        return a * power(a*a, n/2);
    }
}

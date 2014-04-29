package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.data.Rational;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
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
    public HalNumber pow(HalNumber n) {
        if(n instanceof HalInteger) {
            int p = n.toInteger();
            return new HalRational(new Rational(
                    HalInteger.power(value.getNum(), p),
                    HalInteger.power(value.getDen(), p)
            ));
        }

        return new HalFloat(Math.pow(toFloat(), n.toFloat()));
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
 
    private static final Reference den = new Reference(new BuiltinMethod("den") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            return new HalInteger(((Rational)instance.value).getDen());
        }
    });
    
    private static final Reference num = new Reference(new BuiltinMethod("num") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            return new HalInteger(((Rational)instance.value).getNum());
        }
    });
    
    public static final HalClass klass = new HalClass("Rational", HalNumber.klass, 
            den,
            num
    );
}

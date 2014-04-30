package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.ZeroDivisionException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalString;
import java.math.BigInteger;


public abstract class HalNumber<T extends Number> extends HalObject<T> {
    public HalNumber(T i) {
        super(i);
    }

    public abstract boolean isZero();

    public Integer toInteger() { return value.intValue(); }
    public Double toFloat() { return value.doubleValue(); }

    public HalBoolean bool() { return new HalBoolean(!isZero()); }

    public HalNumber pos() { return this; }
    public abstract HalNumber neg();

    public HalString str() { return new HalString(value.toString()); }

    public abstract boolean canCoerce(HalObject n);
    public abstract HalNumber coerce(HalObject n);

    public abstract HalNumber add(HalNumber n);
    public abstract HalNumber sub(HalNumber n);
    public abstract HalNumber mul(HalNumber n);
    public abstract HalNumber pow(HalNumber n);
    public abstract HalNumber div(HalNumber n);
    public HalNumber mod(HalNumber n) { throw new NameException("__mod__"); }
    public HalNumber ddiv(HalNumber n) { throw new NameException("__ddiv__"); }

    public HalNumber radd(HalNumber n) { return n.add(this); }
    public HalNumber rsub(HalNumber n) { return n.sub(this); }
    public HalNumber rmul(HalNumber n) { return n.mul(this); }
    public HalNumber rdiv(HalNumber n) { return n.div(this); }
    public HalNumber rpow(HalNumber n) { return n.pow(this); }
    public HalNumber rmod(HalNumber n) { return n.rmod(this); }
    public HalNumber rddiv(HalNumber n) { return n.rddiv(this); }

    public abstract HalBoolean eq(HalNumber n);
    public abstract HalBoolean lt(HalNumber n);


    private static final Reference __int__ = new Reference(new BuiltinMethod("int") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();

            return new HalInteger(((HalNumber) instance).toInteger());
        }
    });

    private static final Reference __float__ = new Reference(new BuiltinMethod("float") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();

            return new HalFloat(((HalNumber) instance).toFloat());
        }
    });

    // Unary
    private static final Reference __neg__ = new Reference(new BuiltinMethod("neg") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();

            return ((HalNumber) instance).neg();
        }
    });

    private static final Reference __pos__ = new Reference(new BuiltinMethod("pos") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();

            return ((HalNumber) instance).pos();
        }
    });

    // Binary left
    private static final Reference __add__ = new Reference(new BuiltinMethod("add") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1)
                throw new InvalidArgumentsException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce(args[0]))
                return args[0].methodcall("__radd__", i);

            return i.add(i.coerce((HalNumber) args[0]));
        }
    });

    private static final Reference __sub__ = new Reference(new BuiltinMethod("sub") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !(args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce((HalNumber) args[0]))
                return args[0].methodcall("__rsub__", i);

            return i.sub(i.coerce((HalNumber) args[0]));
        }
    });

    private static final Reference __mul__ = new Reference(new BuiltinMethod("mul") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !(args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce((HalNumber) args[0]))
                return args[0].methodcall("__rmul__", i);

            return i.mul(i.coerce((HalNumber) args[0]));
        }
    });

    private static final Reference __div__ = new Reference(new BuiltinMethod("div") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !(args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();

            if (((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce((HalNumber) args[0]))
                return args[0].methodcall("__rdiv__", i);

            return i.div(i.coerce((HalNumber) args[0]));
        }
    });

    private static final Reference __ddiv__ = new Reference(new BuiltinMethod("ddiv") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !(args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();

            if (((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce((HalNumber) args[0]))
                return args[0].methodcall("__rddiv__", i);

            return i.ddiv(i.coerce((HalNumber) args[0]));
        }
    });

    private static final Reference __mod__ = new Reference(new BuiltinMethod("mod") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1)
                throw new InvalidArgumentsException();

            if (((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce((HalNumber) args[0]))
                return args[0].methodcall("__rmod__", i);
            return i.mod(i.coerce((HalNumber) args[0]));
        }
    });

    private static final Reference __pow__ = new Reference(new BuiltinMethod("pow") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1)
                throw new InvalidArgumentsException();
            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce((HalNumber) args[0]))
                return args[0].methodcall("__rpow__", i);

            return i.pow(i.coerce((HalNumber) args[0]));
        }
    });

    // Binary right (fallbacks for when left version fails)
    private static final Reference __radd__ = new Reference(new BuiltinMethod("radd") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            HalNumber i = (HalNumber) instance;
            return i.radd(i.coerce(args[0]));
        }
    });

    private static final Reference __rsub__ = new Reference(new BuiltinMethod("rsub") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            HalNumber i = (HalNumber) instance;
            return i.rsub(i.coerce(args[0]));
        }
    });

    private static final Reference __rmul__ = new Reference(new BuiltinMethod("rmul") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            HalNumber i = (HalNumber) instance;
            return i.rmul(i.coerce(args[0]));
        }
    });

    private static final Reference __rdiv__ = new Reference(new BuiltinMethod("rdiv") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            if (((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            HalNumber i = (HalNumber) instance;
            return i.rdiv(i.coerce(args[0]));
        }
    });

    private static final Reference __rddiv__ = new Reference(new BuiltinMethod("rddiv") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            if (((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            HalNumber i = (HalNumber) instance;
            return i.rddiv(i.coerce(args[0]));
        }
    });

    private static final Reference __rmod__ = new Reference(new BuiltinMethod("rmod") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            if (((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            HalNumber i = (HalNumber) instance;
            return i.rmod(i.coerce(args[0]));
        }
    });

    private static final Reference __rpow__ = new Reference(new BuiltinMethod("rpow") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1 || !((HalNumber) instance).canCoerce((HalNumber) args[0]))
                throw new InvalidArgumentsException();

            HalNumber i = (HalNumber) instance;
            return i.rpow(i.coerce(args[0]));
        }
    });

    private static final Reference __lt__ = new Reference(new BuiltinMethod("lt") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1)
                throw new InvalidArgumentsException();

            return ((HalNumber) instance).lt(((HalNumber) args[0]));
        }
    });

    private static final Reference __eq__ = new Reference(new BuiltinMethod("eq") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 1)
                throw new InvalidArgumentsException();

            return ((HalNumber) instance).eq(((HalNumber) args[0]));
        }
    });

    public static final HalClass klass = new HalClass("Number", HalObject.klass,
            // Conversion
            __int__,
            __float__,

            // Unary
            __neg__,
            __pos__,

            // Binary
            __add__,
            __sub__,
            __mul__,
            __div__,
            __ddiv__,
            __mod__,
            __pow__,
            __radd__,
            __rsub__,
            __rmul__,
            __rdiv__,
            __rddiv__,
            __rmod__,
            __rpow__,

            // Relational
            __lt__,
            __eq__
    );
}

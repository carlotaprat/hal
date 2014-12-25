package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.NewNotSupportedException;
import hal.interpreter.exceptions.ZeroDivisionException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalString;


public abstract class HalNumber<T extends Number> extends HalObject<T> {
    public HalNumber(T i) {
        super(i);
    }

    public abstract boolean isZero();

    public Integer intValue() { return value.intValue(); }
    public Double doubleValue() { return value.doubleValue(); }
    public Object toFormat() { return value; }

    public HalBoolean bool() { return new HalBoolean(!isZero()); }

    public HalNumber pos() { return this; }
    public abstract HalNumber neg();
    public abstract HalNumber abs();

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


    private static final Reference __int__ = new Reference(new Builtin("int") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalInteger(((HalNumber) instance).intValue());
        }
    });

    private static final Reference __float__ = new Reference(new Builtin("float") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalFloat(((HalNumber) instance).doubleValue());
        }
    });

    // Unary
    private static final Reference __neg__ = new Reference(new Builtin("neg") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalNumber) instance).neg();
        }
    });

    private static final Reference __pos__ = new Reference(new Builtin("pos") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalNumber) instance).pos();
        }
    });

    private static final Reference __abs__ = new Reference(new Builtin("abs") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalNumber) instance).abs();
        }
    });

    // Binary left
    private static final Reference __add__ = new Reference(new Builtin("add", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = ((HalNumber) instance);
            HalObject x = args.get("x");

            if (!i.canCoerce(x))
                return x.methodcall("__radd__", i);

            return i.add(i.coerce(x));
        }
    });

    private static final Reference __sub__ = new Reference(new Builtin("sub", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = ((HalNumber) instance);
            HalObject x = args.get("x");

            if (!i.canCoerce(x))
                return x.methodcall("__rsub__", i);

            return i.sub(i.coerce(x));
        }
    });

    private static final Reference __mul__ = new Reference(new Builtin("mul", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = ((HalNumber) instance);
            HalObject x = args.get("x");

            if (!i.canCoerce(x))
                return x.methodcall("__rmul__", i);

            return i.mul(i.coerce(x));
        }
    });

    private static final Reference __div__ = new Reference(new Builtin("div", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");

            if (x.isZero())
                throw new ZeroDivisionException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce(x))
                return x.methodcall("__rdiv__", i);

            return i.div(i.coerce(x));
        }
    });

    private static final Reference __ddiv__ = new Reference(new Builtin("ddiv", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");

            if (x.isZero())
                throw new ZeroDivisionException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce(x))
                return x.methodcall("__rddiv__", i);

            return i.ddiv(i.coerce(x));
        }
    });

    private static final Reference __mod__ = new Reference(new Builtin("mod", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");

            if (x.isZero())
                throw new ZeroDivisionException();

            HalNumber i = ((HalNumber) instance);
            if (!i.canCoerce(x))
                return x.methodcall("__rmod__", i);
            return i.mod(i.coerce(x));
        }
    });

    private static final Reference __pow__ = new Reference(new Builtin("pow", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");
            HalNumber i = ((HalNumber) instance);

            if (!i.canCoerce(x))
                return x.methodcall("__rpow__", i);

            return i.pow(i.coerce(x));
        }
    });

    // Binary right (fallbacks for when left version fails)
    private static final Reference __radd__ = new Reference(new Builtin("radd", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = (HalNumber) instance;
            return i.radd(i.coerce(args.get("x")));
        }
    });

    private static final Reference __rsub__ = new Reference(new Builtin("rsub", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = (HalNumber) instance;
            return i.rsub(i.coerce(args.get("x")));
        }
    });

    private static final Reference __rmul__ = new Reference(new Builtin("rmul", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = (HalNumber) instance;
            return i.rmul(i.coerce(args.get("x")));
        }
    });

    private static final Reference __rdiv__ = new Reference(new Builtin("rdiv", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");

            if (x.isZero())
                throw new ZeroDivisionException();

            HalNumber i = (HalNumber) instance;
            return i.rdiv(i.coerce(x));
        }
    });

    private static final Reference __rddiv__ = new Reference(new Builtin("rddiv", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");

            if (x.isZero())
                throw new ZeroDivisionException();

            HalNumber i = (HalNumber) instance;
            return i.rddiv(i.coerce(x));
        }
    });

    private static final Reference __rmod__ = new Reference(new Builtin("rmod", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber x = (HalNumber) args.get("x");

            if (x.isZero())
                throw new ZeroDivisionException();

            HalNumber i = (HalNumber) instance;
            return i.rmod(i.coerce(x));
        }
    });

    private static final Reference __rpow__ = new Reference(new Builtin("rpow", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalNumber i = (HalNumber) instance;
            return i.rpow(i.coerce(args.get("x")));
        }
    });

    private static final Reference __lt__ = new Reference(new Builtin("lt", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalNumber) instance).lt(((HalNumber) args.get("x")));
        }
    });

    private static final Reference __eq__ = new Reference(new Builtin("eq", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject x = args.get("x");

            if(x instanceof HalNumber)
                return ((HalNumber) instance).eq(((HalNumber) args.get("x")));
            else
                return new HalBoolean(false);
        }
    });

    public static final HalClass klass = new HalClass("Number", HalObject.klass,
            // Conversion
            __int__,
            __float__,

            // Unary
            __neg__,
            __pos__,
            __abs__,

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
    ) {
        public HalObject newInstance(HalClass instklass) {
            throw new NewNotSupportedException();
        }
    };
}

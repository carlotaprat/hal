package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.ZeroDivisionException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalString;


public abstract class HalNumber<T extends Number> extends HalObject<T> {
    public HalNumber(T i) {
        super(i);
    }

    public abstract boolean isZero();
    public int toInteger() { return value.intValue(); }
    public Double toFloat() { return value.doubleValue(); }
    public HalBoolean bool() { return new HalBoolean(!isZero()); }

    public HalNumber pos() { return this; }
    public abstract HalNumber neg();

    public HalString str() {
        return new HalString(value.toString());
    }

    public abstract HalNumber add(HalNumber n);
    public abstract HalNumber sub(HalNumber n);
    public abstract HalNumber mul(HalNumber n);
    public abstract HalNumber div(HalNumber n);
    public HalNumber mod(HalNumber n) { throw new InvalidArgumentsException(); }
    public HalNumber ddiv(HalNumber n) { throw new InvalidArgumentsException(); }

    public abstract HalBoolean eq(HalNumber n);
    public abstract HalBoolean lt(HalNumber n);
    
    private static final Reference __int__ = new Reference(new BuiltinMethod("int") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            return new HalInteger(((HalNumber)instance).toInteger());
        }
    });

    private static final Reference __float__ = new Reference(new BuiltinMethod("float") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            return new HalFloat(((HalNumber)instance).toFloat());
        }
    });

    // Unary
    private static final Reference __neg__ = new Reference(new BuiltinMethod("neg") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            return ((HalNumber)instance).neg();
        }
    });

    private static final Reference __pos__ = new Reference(new BuiltinMethod("pos") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            return ((HalNumber)instance).pos();
        }
    });

    // Binary
    private static final Reference __add__ = new Reference(new BuiltinMethod("add") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            return ((HalNumber)instance).add((HalNumber)args[0]);
        }
    });

    private static final Reference __sub__ = new Reference(new BuiltinMethod("sub") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1 || ! (args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();

            return ((HalNumber)instance).sub((HalNumber)args[0]);
        }
    });

    private static final Reference __mul__ = new Reference(new BuiltinMethod("mul") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1 || ! (args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();

            return ((HalNumber)instance).mul((HalNumber)args[0]);
        }
    });

    private static final Reference __div__ = new Reference(new BuiltinMethod("div") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1 || ! (args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();
            if(((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            return ((HalNumber)instance).div((HalNumber)args[0]);
        }
    });
    
    private static final Reference __ddiv__ = new Reference(new BuiltinMethod("ddiv") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1 || ! (args[0] instanceof HalNumber))
                throw new InvalidArgumentsException();
            if(((HalNumber) args[0]).isZero())
                throw new ZeroDivisionException();

            return ((HalNumber)instance).ddiv((HalNumber)args[0]);
        }
    });


    private static final Reference __lt__ = new Reference(new BuiltinMethod("lt") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            return ((HalNumber)instance).lt(((HalNumber)args[0]));
        }
    });
    
    private static final Reference __eq__ = new Reference(new BuiltinMethod("eq") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            return ((HalNumber)instance).eq(((HalNumber)args[0]));
        }
    });
    
    
    public static final ReferenceRecord record = new ReferenceRecord("Number", HalObject.record,
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

            // Relational
            __lt__,
            __eq__
    );

    public ReferenceRecord getRecord() { return record; }
}

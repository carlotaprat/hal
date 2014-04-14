package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.enumerable.HalString;

public abstract class HalType
{
    public HalString str() {
        throw new NameException("__str__");
    }

    public abstract HalBoolean bool();
    public abstract HalBoolean not();
    public abstract HalClass getKlass();
    public ReferenceRecord getInstanceRecord() {
        throw new TypeException("Definition outside a class.");
    }

    private static Reference __repr__ = new Reference(new BuiltinMethod("repr") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new TypeException();

            return instance.repr();
        }
    });

    private static Reference __str__ = new Reference(new BuiltinMethod("str") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new InvalidArgumentsException();

            return instance.str();
        }
    });

    private static final Reference __bool__ = new Reference(new BuiltinMethod("bool") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new InvalidArgumentsException();

            return instance.bool();
        }
    });

    private static final Reference __not__ = new Reference(new BuiltinMethod("not") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new InvalidArgumentsException();

            return instance.not();
        }
    });

    private static Reference __eq__ = new Reference(new BuiltinMethod("eq") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            return new HalBoolean(instance.getValue().equals(args[0].getValue()));
        }
    });

    private static Reference __neq__ = new Reference(new BuiltinMethod("neq") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return instance.methodcall("__eq__", args).methodcall("__not__");
        }
    });

    private static final Reference __le__ = new Reference(new BuiltinMethod("le") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalBoolean(instance.methodcall("__lt__", args).toBoolean() ||
                    instance.methodcall("__eq__", args).toBoolean());
        }
    });

    private static final Reference __gt__ = new Reference(new BuiltinMethod("gt") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return instance.methodcall("__le__", args).methodcall("__not__");
        }
    });

    private static final Reference __ge__ = new Reference(new BuiltinMethod("ge") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return instance.methodcall("__lt__", args).methodcall("__not__");
        }
    });

    public static final ReferenceRecord record = new ReferenceRecord("Type", null,
            __repr__,
            __str__,
            __bool__,
            __not__,
            __eq__,
            __neq__,
            __le__,
            __gt__,
            __ge__
    );
}

package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalString;

public abstract class HalType
{
    public HalString str() {
        throw new NameException("__str__");
    }

    public abstract HalBoolean bool();
    public abstract HalBoolean not();
    public abstract HalClass getKlass();

    private static final Reference __init__ = new Reference(new Builtin("init") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance;
        }
    });

    private static Reference __repr__ = new Reference(new Builtin("repr") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.repr();
        }
    });

    private static Reference __str__ = new Reference(new Builtin("str") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.str();
        }
    });

    private static final Reference __bool__ = new Reference(new Builtin("bool") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.bool();
        }
    });

    private static final Reference __not__ = new Reference(new Builtin("not") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.not();
        }
    });

    private static Reference __eq__ = new Reference(new Builtin("eq", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalBoolean(instance.getValue().equals(args.get("x").getValue()));
        }
    });

    private static Reference __neq__ = new Reference(new Builtin("neq") {
        @Override
        public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.methodcall("__eq__", args).methodcall("__not__");
        }
    });

    private static final Reference __le__ = new Reference(new Builtin("le") {
        @Override
        public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalBoolean(instance.methodcall("__lt__", args).toBoolean() ||
                    instance.methodcall("__eq__", args).toBoolean());
        }
    });

    private static final Reference __gt__ = new Reference(new Builtin("gt") {
        @Override
        public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.methodcall("__le__", args).methodcall("__not__");
        }
    });

    private static final Reference __ge__ = new Reference(new Builtin("ge") {
        @Override
        public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.methodcall("__lt__", args).methodcall("__not__");
        }
    });

    private static final Reference __none__ = new Reference(new Builtin("none?") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalBoolean(instance == HalNone.NONE);
        }
    });

    private static final Reference __instance_exec__ = new Reference(new Builtin("instance_exec",
            new Params.ParamGroup("args")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray margs = (HalArray) args.get("args");
            HalLambda l = (HalLambda) margs.value.remove(margs.value.size()-1);

            return l.instanceEval(instance, null, new Arguments(margs));
        }
    });

    public static final HalClass klass = new HalClass("Type", null,
            __init__,
            __repr__,
            __str__,
            __bool__,
            __not__,
            __eq__,
            __neq__,
            __le__,
            __gt__,
            __ge__,
            __none__,
            __instance_exec__);
}

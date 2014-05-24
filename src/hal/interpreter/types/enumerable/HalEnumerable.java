package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.InternalLambda;
import hal.interpreter.core.Params;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;


public abstract class HalEnumerable<T> extends HalObject<T>
{
    public abstract HalObject getitem(HalObject index);

    public void setitem(HalObject index, HalObject item) {
        throw new NameException("__setitem__");
    }

    public abstract HalInteger size();

    public HalBoolean bool() {
        return new HalBoolean(size().value != 0);
    }

    private static final Reference __getitem__ = new Reference(new Builtin("getitem", new Params.Param("key")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalEnumerable) instance).getitem(args.get("key"));
        }
    });

    private static final Reference __setitem__ = new Reference(new Builtin("setitem",
            new Params.Param("key"),
            new Params.Param("value"))
    {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject value = args.get("value");
            ((HalEnumerable) instance).setitem(args.get("key"), value);
            return value;
        }
    });

    private static final Reference __size__ = new Reference(new Builtin("size") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalEnumerable) instance).size();
        }
    });

    private static final Reference __length__ = new Reference(new Builtin("length") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.methodcall("__size__");
        }
    });

    private static final Reference __map__ = new Reference(new Builtin("map") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            final HalArray n = new HalArray();
            final HalMethod f = lambda;

            instance.methodcall_lambda("__each__", new InternalLambda(new Params.Param("x")) {
                @Override
                public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                    return n.methodcall("__append!__", f.call(n, null, args));
                }
            });

            return n;
        }
    });

    public static final HalClass klass = new HalClass("Enumerable", HalObject.klass,
            __getitem__,
            __setitem__,
            __size__,
            __length__,
            __map__
    );
}

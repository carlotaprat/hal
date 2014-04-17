package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
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

    private static final Reference __getitem__ = new Reference(new BuiltinMethod("getitem") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            return ((HalEnumerable) instance).getitem(args[0]);
        }
    });

    private static final Reference __setitem__ = new Reference(new BuiltinMethod("setitem") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length != 2)
                throw new InvalidArgumentsException();

            ((HalEnumerable) instance).setitem(args[0], args[1]);
            return args[1];
        }
    });

    private static final Reference __size__ = new Reference(new BuiltinMethod("size") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length > 0)
                throw new InvalidArgumentsException();

            return ((HalEnumerable) instance).size();
        }
    });

    private static final Reference __length__ = new Reference(new BuiltinMethod("length") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            return instance.methodcall("__size__");
        }
    });

    public static final HalClass klass = new HalClass("Enumerable", HalObject.klass,
            __getitem__,
            __setitem__,
            __size__,
            __length__
    );
}

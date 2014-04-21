package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalNone;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;

import java.util.ArrayList;
import java.util.List;


public class HalArray extends HalEnumerable<List<HalObject>>
{
    public HalArray() {
        value = new ArrayList<HalObject>();
    }

    public HalString str() {
        String s = "";
        boolean first = true;

        for(HalObject element : value) {
            if(first) first = false;
            else s += ", ";

            s += element.methodcall("__repr__").getValue();
        }

        return new HalString("[" + s + "]");
    }

    public HalObject getitem(HalObject index) {
        return value.get(((HalInteger) index).value);
    }

    public void setitem(HalObject index, HalObject item) {
        value.set(((HalInteger) index).value, item);
    }

    public HalInteger size() {
        return new HalInteger(value.size());
    }


    private static final Reference __append__ = new Reference(new BuiltinMethod("append!") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            ((HalArray) instance).value.add(args[0]);
            return instance;
        }
    });

    private static final Reference __lshift__ = new Reference(new BuiltinMethod("lshift") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length != 1)
                throw new InvalidArgumentsException();

            return instance.methodcall("__append!__", args);
        }
    });

    private static final Reference __sum__ = new Reference(new BuiltinMethod("sum") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length > 0)
                throw new InvalidArgumentsException();

            HalObject sum = new HalInteger(0);

            HalArray i = (HalArray) instance;
            for(HalObject element : i.value)
                sum = sum.methodcall("__add__", element);

            return sum;
        }
    });
    
    private static final Reference __each__ = new Reference(new BuiltinMethod("each") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            
            HalObject last = HalNone.NONE;
            HalArray i = (HalArray) instance;
            for (HalObject element: i.value) {
                last = lambda.call(instance, null, element);
            }
            return last;
        }
    });

    public static final HalClass klass = new HalClass("Array", HalEnumerable.klass,
            __append__,
            __lshift__,
            __sum__,
            __each__
    );

    public HalClass getKlass() { return HalArray.klass; }

}

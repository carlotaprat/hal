package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
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

    public HalObject[] flatten() {
        int n = value.size();
        HalObject[] flat = new HalObject[n];

        for(int i = 0; i < n; ++i)
            flat[i] = value.get(i);

        return flat;
    }

    private static final Reference __append__ = new Reference(new Builtin("append!", new Params.Param("element")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            ((HalArray) instance).value.add(args.get("element"));
            return instance;
        }
    });
    
    private static final Reference __pop__ = new Reference(new Builtin("pop!") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            ((HalArray) instance).value.remove(0);
            return instance;
        }
    });

    private static final Reference __lshift__ = new Reference(new Builtin("lshift") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return instance.methodcall("__append!__", args);
        }
    });

    private static final Reference __sum__ = new Reference(new Builtin("sum") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject sum = new HalInteger(0);

            HalArray i = (HalArray) instance;
            for(HalObject element : i.value)
                sum = sum.methodcall("__add__", element);

            return sum;
        }
    });
    
    private static final Reference __each__ = new Reference(new Builtin("each") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject last = HalNone.NONE;
            HalArray i = (HalArray) instance;
            for (HalObject element: i.value) {
                last = lambda.call(instance, null, element);
            }
            return last;
        }
    });

    public static final HalClass klass = new HalClass("Array", false, HalEnumerable.klass,
            __append__,
            __pop__,
            __lshift__,
            __sum__,
            __each__
    ) {
        public HalObject newInstance(final HalClass instklass) {
            return new HalArray() {
                public HalClass getKlass() { return instklass; }
            };
        }
    };

    public HalClass getKlass() { return HalArray.klass; }

}

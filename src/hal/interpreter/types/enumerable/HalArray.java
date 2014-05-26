package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.InternalLambda;
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

    private static final Reference __append__ = new Reference(new Builtin("append!", new Params.Param("element")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            ((HalArray) instance).value.add(args.get("element"));
            return instance;
        }
    });

    private static final Reference __first__ = new Reference(new Builtin("first") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalArray) instance).value.get(0);
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

    private static final Reference __filter__ = new Reference(new Builtin("filter") {
        @Override
        public HalObject mcall(HalObject instance, final HalMethod lambda, Arguments args) {
            final HalObject en = instance.getKlass().newInstance(instance.getKlass());

            instance.methodcall_lambda("__each__", new InternalLambda(new Params.Param("x")) {
                public HalObject mcall(HalObject instance, HalMethod l, Arguments args) {
                    HalObject ret = lambda.call(null, null, args.get("x"));

                    if(ret.toBoolean())
                        en.methodcall("__append!__", args.get("x"));

                    return ret;
                }
            });

            return en;
        }
    });

    private static final Reference __concat__ = new Reference(new Builtin("concat", new Params.Param("x")) {
        @Override
        public HalObject mcall(final HalObject instance, HalMethod lambda, Arguments args) {
            HalObject xs = args.get("x");

            xs.methodcall_lambda("__each__", new InternalLambda(new Params.Param("x")) {
                public HalObject call(HalObject i, HalMethod l, Arguments args) {
                    return instance.methodcall("__append!__", args);
                }
            });

            return instance;
        }
    });

    public static final HalClass klass = new HalClass("Array", HalEnumerable.klass,
            __append__,
            __first__,
            __pop__,
            __lshift__,
            __sum__,
            __each__,
            __filter__,
            __concat__
    ) {
        public HalObject newInstance(final HalClass instklass) {
            return new HalArray() {
                public HalClass getKlass() { return instklass; }
            };
        }
    };

    public HalClass getKlass() { return HalArray.klass; }

}

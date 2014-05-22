package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.exceptions.KeyException;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalNone;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;

import java.util.HashMap;
import java.util.Map;


public class HalDictionary extends HalEnumerable<HashMap<HalObject, HalObject>>
{
    
    public HalDictionary() {
        value = new HashMap<HalObject, HalObject>();
    }

    public HalString str() {
        String s = "";
        boolean first = true;

        for(Map.Entry<HalObject, HalObject> e : value.entrySet()) {
            if(first) first = false;
            else s += ", ";

            s += e.getKey().methodcall("__repr__") + " => " + e.getValue().methodcall("__repr__");
        }

        return new HalString("{" + s + "}");
    }

    public HalObject getitem(HalObject index) {
        HalObject item = value.get(index);

        if(item == null)
            throw new KeyException("Invalid key " + index.methodcall("__repr__"));

        return item;
    }

    public void setitem(HalObject index, HalObject item) {
        value.put(index, item);
    }

    public HalInteger size() {
        return new HalInteger(value.size());
    }
    
    public HalArray keys() {
        HalArray arr = new HalArray();
        for(Map.Entry<HalObject, HalObject> e : value.entrySet()) {
            arr.methodcall("__append!__", e.getKey());
        }
        return arr;
    }
    
    public HalArray values() {
        HalArray arr = new HalArray();
        for(Map.Entry<HalObject, HalObject> e : value.entrySet()) {
            arr.methodcall("__append!__", e.getValue());
        }
        return arr; 
    }
    
    private static final Reference __keys__ = new Reference(new Builtin("keys") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalDictionary) instance).keys();
        }
    });
    
    private static final Reference __values__ = new Reference(new Builtin("values") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return ((HalDictionary) instance).values();
        }
    });
    
    private static final Reference __each__ = new Reference(new Builtin("each") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject last = HalNone.NONE;
            HalDictionary d = (HalDictionary) instance;
            for(Map.Entry<HalObject, HalObject> e : d.value.entrySet()) {
                last = lambda.call(instance, null, e.getKey(), e.getValue());
            }
            return last;
        }
    });
    
    public static final HalClass klass = new HalClass("Dictionary", HalEnumerable.klass,
            __each__,
            __keys__,
            __values__
    ){
        public HalObject newInstance(final HalClass instklass) {
            return new HalDictionary() {
                public HalClass getKlass() { return instklass; }
            };
        }
    };

    public HalClass getKlass() { return HalDictionary.klass; }
}

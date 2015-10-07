package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.exceptions.KeyException;
import hal.interpreter.types.*;
import hal.interpreter.types.numeric.HalInteger;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;


public class HalDictionary extends HalEnumerable<HashMap<HalObject, HalObject>>
{
    private ArrayList<HalObject> keys;

    public HalDictionary() {
        value = new HashMap<HalObject, HalObject>();
        keys = new ArrayList<HalObject>();
    }

    public HalString str() {
        String s = "";
        boolean first = true;

        for(HalObject key : keys) {
            if(first) first = false;
            else s += ", ";

            s += key.methodcall("__repr__") + " => " + value.get(key).methodcall("__repr__");
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
        if(! value.containsKey(index)) {
            keys.add(index);
        }

        value.put(index, item);
    }

    public HalInteger size() {
        return new HalInteger(value.size());
    }
    
    public HalArray keys() {
        HalArray arr = new HalArray();

        for(HalObject key : keys) {
            arr.methodcall("__append!__", key);
        }

        return arr;
    }
    
    public HalArray values() {
        HalArray arr = new HalArray();

        for(HalObject key : keys) {
            arr.methodcall("__append!__", value.get(key));
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

            for(HalObject key : d.keys) {
                last = lambda.call(instance, null, key, d.value.get(key));
            }
            
            return last;
        }
    });

    private static final Reference __has_key__ = new Reference(new Builtin("has_key?", new Params.Param("key")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalBoolean(((HalDictionary)instance).value.containsKey(args.get("key")));
        }
    });
    
    public static final HalClass klass = new HalClass("Dictionary", HalEnumerable.klass,
            __each__,
            __keys__,
            __values__,
            __has_key__
    ){
        public HalObject newInstance(final HalClass instklass) {
            return new HalDictionary() {
                public HalClass getKlass() { return instklass; }
            };
        }
    };

    public HalClass getKlass() { return HalDictionary.klass; }
}

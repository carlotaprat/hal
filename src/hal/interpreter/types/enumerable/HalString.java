package hal.interpreter.types.enumerable;


import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalNone;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;
import java.util.Iterator;

public class HalString extends HalEnumerable<String>
{

    public HalString(Character c) {
        value = String.valueOf(c);
    }
    
    public HalString(String s) {
        value = s;
    }

    public String toString(){
        return value;
    }

    public HalString repr() {
        return new HalString("'" + value + "'");
    }

    public HalString str() {
        return new HalString(value);
    }

    public HalString getitem(HalObject index) {
        HalInteger i = (HalInteger) index;

        return new HalString(value.substring(i.value, i.value+1));
    }

    public HalInteger size() {
        return new HalInteger(value.length());
    }
    
    private static final Reference __each__ = new Reference(new BuiltinMethod("each") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if (args.length != 0)
                throw new InvalidArgumentsException();
            
            HalObject last = HalNone.NONE;
            HalString s = (HalString) instance;
            for (int i=0; i < s.value.length(); i++) {
                last = lambda.call(instance, null, new HalString(s.value.charAt(i)));
            }
            return last;
        }
    });
    
    public static final HalClass klass = new HalClass("String", HalEnumerable.klass,
            __each__
    );
    
    public HalClass getKlass() { return klass; }

}

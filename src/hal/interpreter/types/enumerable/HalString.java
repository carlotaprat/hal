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
    
    private static final Reference __each__ = new Reference(new Builtin("each") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject last = HalNone.NONE;
            HalString s = (HalString) instance;
            for (int i=0; i < s.value.length(); i++) {
                last = lambda.call(instance, null, new HalString(s.value.charAt(i)));
            }
            return last;
        }
    });

    private static final Reference __add__ = new Reference(new Builtin("add", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalString(((HalString)instance).value + ((HalString)args.get("x")).value);
        }
    });

    private static final Reference __mod__ = new Reference(new Builtin("mod", new Params.Param("x")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalString(String.format(((HalString)instance).value, args.get("x")));
        }
    });

    private static final Reference __gsub__ = new Reference(new Builtin("gsub",
            new Params.Param("pattern"),
            new Params.Param("replace"))
    {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            String s = ((HalString)instance).value.replaceAll(
                    args.get("pattern").toString(),
                    args.get("replace").toString());

            return new HalString(s);
        }
    });
    
    public static final HalClass klass = new HalClass("String", HalEnumerable.klass,
            __each__,
            __add__,
            __mod__,
            __gsub__
    );
    
    public HalClass getKlass() { return klass; }
}

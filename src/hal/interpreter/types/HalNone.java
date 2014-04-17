package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.types.enumerable.HalString;

public class HalNone extends HalObject
{
    public HalNone() {
        value = null;
    }

    public HalString str() {
        return new HalString("none");
    }

    public HalBoolean bool() {
        return new HalBoolean(false);
    }

    public HalClass getKlass() { return HalNone.klass; }
    
    private static final Reference __eq__ = new Reference(new BuiltinMethod("eq") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            return new HalBoolean(args[0] instanceof HalNone); 
        }
    });

    public static final HalClass klass = new HalClass("None", HalObject.klass, __eq__);
    public static final HalNone NONE = new HalNone();
}

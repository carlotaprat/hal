package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
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
    
    private static final Reference __eq__ = new Reference(new Builtin("eq", new Params.Param("e1")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            return new HalBoolean(args.get("e1") instanceof HalNone);
        }
    });

    public static final HalClass klass = new HalClass("None", HalObject.klass, __eq__);
    public static final HalNone NONE = new HalNone();
}

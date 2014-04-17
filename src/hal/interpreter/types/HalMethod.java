package hal.interpreter.types;

import hal.Hal;
import hal.interpreter.core.MethodDefinition;
import hal.interpreter.types.enumerable.HalString;


public class HalMethod extends HalObject<MethodDefinition>
{
    public HalMethod(MethodDefinition def) {
        value = def;
    }

    public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
        return Hal.INTERPRETER.executeMethod(value, instance, lambda, args);
    }

    public HalString str() { return new HalString(value.name); }
    public HalBoolean bool() { return new HalBoolean(true); }
    public HalClass getKlass() { return HalMethod.klass; }

    public static final HalClass klass = new HalClass("Method", null);
}

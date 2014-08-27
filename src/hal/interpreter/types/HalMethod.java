package hal.interpreter.types;

import hal.interpreter.core.Arguments;
import hal.interpreter.core.MethodDefinition;
import hal.interpreter.types.enumerable.HalString;


abstract public class HalMethod extends HalObject<MethodDefinition>
{
    public HalMethod(MethodDefinition def) {
        super(def);
    }

    public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
        return mcall(instance, lambda, value.params.fill(args));
    }

    public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
        throw new RuntimeException("mcall not implemented");
    }

    public String getName() {
        return value.name;
    }

    public int getArity() {
        return value.getArity();
    }

    public HalString str() { return new HalString(value.name); }
    public HalBoolean bool() { return new HalBoolean(true); }
    public HalClass getKlass() { return HalMethod.klass; }

    public static final HalClass klass = new HalClass("Method", null);
}

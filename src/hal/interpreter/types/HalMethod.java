package hal.interpreter.types;

import hal.Hal;
import hal.interpreter.HalTree;
import hal.interpreter.core.MethodDefinition;
import hal.interpreter.types.enumerable.HalString;


public class HalMethod extends HalObject<MethodDefinition>
{
    public HalMethod(HalTree tree) {
        value = new MethodDefinition(tree);
    }

    public HalObject call(HalObject instance, HalObject... args) {
        return Hal.INTERPRETER.executeMethod(value, args);
    }

    public HalString str() { return new HalString(value.name); }
    public HalBoolean bool() { return new HalBoolean(true); }
}

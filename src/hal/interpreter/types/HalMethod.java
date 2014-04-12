package hal.interpreter.types;

import hal.Hal;
import hal.interpreter.HalTree;
import hal.interpreter.core.MethodDefinition;


public class HalMethod extends HalObject<MethodDefinition>
{
    public HalMethod(HalTree tree) {
        value = new MethodDefinition(tree);
    }

    public HalObject call(HalObject instance, HalObject... args) {
        return Hal.INTERPRETER.executeMethod(value, args);
    }

    public HalString __str__() {
        return new HalString(value.name);
    }
}

package hal.interpreter.types;

import hal.Hal;
import hal.interpreter.HalTree;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.MethodDefinition;


public class HalDefinedMethod extends HalMethod {
    private HalTree block;

    public HalDefinedMethod(MethodDefinition def, HalTree block) {
        super(def);
        this.block = block;
    }

    public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
        return Hal.INTERPRETER.executeMethod(value, block, instance, lambda, args);
    }
}

package hal.interpreter.datatypes;

import hal.interpreter.DataType;
import hal.interpreter.HalTree;
import hal.interpreter.core.MethodDefinition;


public class HalMethod extends DataType<MethodDefinition>
{
    public HalMethod(HalTree tree) {
        value = new MethodDefinition(tree);
    }

    public HalMethod toMethod() {
        return this;
    }

    public HalString __str__() {
        return new HalString(value.name);
    }
}

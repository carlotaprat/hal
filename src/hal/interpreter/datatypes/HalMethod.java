package hal.interpreter.datatypes;

import hal.Hal;
import hal.interpreter.DataType;
import hal.interpreter.HalTree;
import hal.interpreter.core.MethodDefinition;


public class HalMethod extends DataType<MethodDefinition>
{
    public HalMethod(HalTree tree) {
        value = new MethodDefinition(tree);
    }

    public DataType call(DataType instance, DataType... args) {
        return Hal.INTERPRETER.executeMethod(value, args);
    }

    public HalString __str__() {
        return new HalString(value.name);
    }
}

package hal.interpreter.datatypes;

import hal.interpreter.DataType;
import hal.interpreter.HalTree;
import hal.interpreter.Interpreter;
import hal.interpreter.core.MethodDefinition;


public class HalMethod extends DataType<MethodDefinition>
{
    public HalMethod(HalTree tree) {
        value = new MethodDefinition(tree);
    }

    public DataType call(Interpreter interp, HalTree args) {
        return interp.executeMethod(value, args);
    }

    public HalString __str__() {
        return new HalString(value.name);
    }
}

package hal.interpreter.core;

import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalString;


abstract public class Builtin extends HalObject<String>
{
    public String className;

    public Builtin(String name) {
        super(name);
    }

    public HalString str() { return new HalString("<Builtin: " + value + ">"); }
    public HalBoolean bool(){ return new HalBoolean(true); }
}

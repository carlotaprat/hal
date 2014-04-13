package hal.interpreter.core;

import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;


abstract public class Builtin extends HalObject<String>
{
    public String className;

    public Builtin(String name) {
        super(name);
    }

    public HalBoolean bool(){ return new HalBoolean(true); }
}

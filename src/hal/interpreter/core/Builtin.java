package hal.interpreter.core;

import hal.interpreter.types.HalObject;


abstract public class Builtin extends HalObject<String>
{
    public Builtin(String name) {
        super(name);
    }
}

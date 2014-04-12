package hal.interpreter.core;

import hal.interpreter.types.HalObject;


abstract public class BuiltinMethod extends Builtin
{
    public BuiltinMethod(String method_name) {
        super(method_name);
    }

    abstract public HalObject call(HalObject instance, HalObject... args);
}

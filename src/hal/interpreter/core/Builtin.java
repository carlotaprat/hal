package hal.interpreter.core;

import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalMethod;


abstract public class Builtin extends HalMethod
{
    public Builtin(String name, Params.Param...params) {
        super(new MethodDefinition(null, null, name, params));
    }

    public HalBoolean bool(){ return new HalBoolean(true); }
}

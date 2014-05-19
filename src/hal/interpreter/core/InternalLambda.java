package hal.interpreter.core;

import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.enumerable.HalString;


abstract public class InternalLambda extends HalMethod
{
    public InternalLambda(Params.Param...params) {
        super(new LambdaDefinition(null, null, params));
    }

    public HalString str() { return new HalString("yield"); }
    public HalBoolean bool() { return new HalBoolean(true); }
    public HalClass getKlass() { return HalMethod.klass; }
}

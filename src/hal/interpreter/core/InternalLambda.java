package hal.interpreter.core;

import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalString;


abstract public class InternalLambda extends HalObject<String>
{
    protected HalObject[] data;

    public InternalLambda(HalObject... data) {
        super("yield");
        this.data = data;
    }

    public HalString str() { return new HalString(value); }
    public HalBoolean bool() { return new HalBoolean(true); }
    public HalClass getKlass() { return HalMethod.klass; }

    abstract public HalObject call(HalObject instance, HalObject lambda, HalObject... args);
}

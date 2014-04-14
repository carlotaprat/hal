package hal.interpreter.types;

import hal.Hal;
import hal.interpreter.HalTree;
import hal.interpreter.core.MethodDefinition;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;


public class HalMethod extends HalObject<MethodDefinition>
{
    public static final HalClass klass = new HalClass("Method") {
        public ReferenceRecord getInstanceRecord() { return HalMethod.record; }
    };

    public HalMethod(MethodDefinition def) {
        value = def;
    }

    public HalObject call(HalObject instance, HalObject... args) {
        return Hal.INTERPRETER.executeMethod(value, instance, args);
    }

    public HalString str() { return new HalString(value.name); }
    public HalBoolean bool() { return new HalBoolean(true); }
    public HalClass getKlass() { return HalMethod.klass; }
}

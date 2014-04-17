package hal.interpreter.types;


import hal.interpreter.types.enumerable.HalString;

public class HalBoolean extends HalObject<Boolean>
{
    public static final HalClass klass = new HalClass("Boolean", HalObject.klass);

    public HalBoolean(Boolean b) {
        super(b);
    }

    public Boolean toBoolean() {
        return value;
    }
    public HalString str() { return new HalString(value ? "true" : "false"); }
    public HalBoolean bool() { return new HalBoolean(value); }
    public HalClass getKlass() { return HalBoolean.klass; }
}

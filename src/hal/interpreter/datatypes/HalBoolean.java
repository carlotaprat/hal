package hal.interpreter.datatypes;

import hal.interpreter.DataType;


public class HalBoolean extends DataType<Boolean>
{
    public HalBoolean(Boolean b)
    {
        super(b);
    }

    public HalBoolean __not__()
    {
        return new HalBoolean(!value);
    }

    public HalString __str__() {
        return new HalString(value ? "true" : "false");
    }

    public HalBoolean __bool__() {
        return new HalBoolean(value);
    }
}

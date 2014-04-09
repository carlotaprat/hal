package hal.interpreter.datatypes;

import hal.interpreter.DataType;

public class HalNone extends DataType
{
    public HalNone()
    {
        value = null;
    }

    public HalString __str__()
    {
       return new HalString("None");
    }
}

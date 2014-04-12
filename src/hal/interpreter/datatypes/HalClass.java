package hal.interpreter.datatypes;

import hal.interpreter.DataType;


public class HalClass<T> extends DataType<Class>
{
    public HalString __str__() {
        return new HalString("<class: " + value.getSimpleName() + ">");
    }
}

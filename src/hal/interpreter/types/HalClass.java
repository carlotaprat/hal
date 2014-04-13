package hal.interpreter.types;


import hal.interpreter.types.enumerable.HalString;

public class HalClass<T> extends HalObject<Class>
{
    public HalString __str__() {
        return new HalString("<class: " + value.getSimpleName() + ">");
    }
}

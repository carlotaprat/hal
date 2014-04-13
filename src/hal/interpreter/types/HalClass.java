package hal.interpreter.types;


import hal.interpreter.types.enumerable.HalString;

public class HalClass<T> extends HalObject<Class>
{
    public HalString str() {
        return new HalString("<class: " + value.getSimpleName() + ">");
    }

    public HalBoolean bool() {
        return new HalBoolean(true);
    }
}

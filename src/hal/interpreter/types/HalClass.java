package hal.interpreter.types;


public class HalClass<T> extends HalObject<Class>
{
    public HalString __str__() {
        return new HalString("<class: " + value.getSimpleName() + ">");
    }
}

package hal.interpreter.core;

import hal.interpreter.DataType;


abstract public class Builtin extends DataType<String>
{
    public Builtin(String name) {
        super(name);
    }
}

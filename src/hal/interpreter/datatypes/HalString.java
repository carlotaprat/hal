package hal.interpreter.datatypes;


import hal.interpreter.DataType;

public class HalString extends DataType<String>
{
    public HalString(String s) {
        value = s;
    }

    public HalString __str__() {
        return new HalString(value);
    }

    public HalString __repr__() {
        return new HalString("\"" + value + "\"");
    }

    public HalString __getitem__(DataType index) {
        int i = index.toInteger();
        return new HalString(value.substring(i, i+1));
    }
}

package hal.interpreter;

import hal.interpreter.types.HalObject;

public class Reference {
    public HalObject data;

    public Reference(HalObject value) {
        data = value;
    }

    public String toString() {
        return data.toString();
    }
}

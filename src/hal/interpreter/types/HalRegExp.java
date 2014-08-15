package hal.interpreter.types;


import hal.interpreter.exceptions.NewNotSupportedException;
import hal.interpreter.types.enumerable.HalString;

public class HalRegExp extends HalObject<String>
{
    public HalRegExp(String s) {
        value = s;
    }

    public HalBoolean bool(){
        return new HalBoolean(true);
    }

    public HalString str() {
        return new HalString(value);
    }

    public HalString repr() {
        return new HalString("/" + value + "/");
    }

    public static final HalClass klass = new HalClass("RegExp", HalObject.klass) {
        public HalObject newInstance(HalClass instklass) {
            throw new NewNotSupportedException();
        }
    };

    public HalClass getKlass(){
        return HalRegExp.klass;
    }
}

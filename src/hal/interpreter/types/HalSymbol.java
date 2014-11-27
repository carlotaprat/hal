package hal.interpreter.types;


import hal.interpreter.types.enumerable.HalString;

public class HalSymbol extends HalObject<String>
{

    public HalSymbol(Character c) {
        value = String.valueOf(c);
    }
    public HalSymbol(String s) {
        value = s;
    }

    public String toString(){
        return ':' + value;
    }

    public HalString repr() {
        return new HalString(toString());
    }

    public HalString str() {
        return repr();
    }

    public HalBoolean bool(){
        return new HalBoolean(true);
    }

    public static final HalClass klass = new HalClass("Symbol", HalObject.klass
    );

    public HalClass getKlass() { return klass; }
}

package hal.interpreter.types;


import hal.interpreter.types.enumerable.HalString;

import java.util.HashMap;

public class HalSymbol extends HalObject<String>
{
    static private HashMap<String, HalSymbol> symbols = new HashMap<String, HalSymbol>();

    static public HalSymbol getSymbol(String s) {
        if(!symbols.containsKey(s)) {
            symbols.put(s, new HalSymbol(s));
        }

        return symbols.get(s);
    }

    private HalSymbol(String s) {
        value = s;
    }

    public String toString(){
        return value;
    }

    public HalString repr() {
        return new HalString(':' + value);
    }

    public HalString str() {
        return new HalString(toString());
    }

    public HalBoolean bool(){
        return new HalBoolean(true);
    }

    public static final HalClass klass = new HalClass("Symbol", HalObject.klass
    );

    public HalClass getKlass() { return klass; }
}

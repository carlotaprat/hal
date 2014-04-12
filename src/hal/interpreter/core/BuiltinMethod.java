package hal.interpreter.core;

import hal.interpreter.DataType;
import hal.interpreter.datatypes.HalString;


abstract public class BuiltinMethod extends Builtin
{
    private static ReferenceRecord cached_record = null;
    private String className;

    public BuiltinMethod(String cname, String method_name) {
        super(method_name);
        className = cname;
    }

    abstract public DataType call(DataType instance, DataType... args);

    public ReferenceRecord createRecord(){
        // Resolve infinite recursion just relaxing the dependency
        if(cached_record != null)
            return cached_record;

        cached_record = new ReferenceRecord();
        return cached_record;
    }

    public HalString __str__() {
        return new HalString("<" + className + "::" + value + " method>");
    }
}

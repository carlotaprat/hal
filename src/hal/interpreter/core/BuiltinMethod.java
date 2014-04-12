package hal.interpreter.core;

import hal.interpreter.types.HalObject;
import hal.interpreter.types.HalString;


abstract public class BuiltinMethod extends Builtin
{
    public static final String classId = "BuiltinMethod";
    private String className;

    public BuiltinMethod(String cname, String method_name) {
        super(method_name);
        className = cname;
    }

    public String getClassId() {
        return classId;
    }

    abstract public HalObject call(HalObject instance, HalObject... args);

    public ReferenceRecord createRecord(){
        // Resolve infinite recursion just relaxing the dependency
        return new ReferenceRecord();
    }

    public HalString __str__() {
        return new HalString("<" + className + "::" + value + " method>");
    }
}

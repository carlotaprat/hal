package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

public class HalString extends HalObject<String>
{
    public static final String classId = "String";

    public HalString(String s) {
        value = s;
    }

    public String getClassId() {
        return classId;
    }

    public String toString(){
        return value;
    }

    public ReferenceRecord createRecord() {
        ReferenceRecord base = super.createRecord();

        base.defineBuiltin(__repr__);
        base.defineBuiltin(__str__);
        base.defineBuiltin(__getitem__);

        return base;
    }

    private static final Reference __repr__ = new Reference(new BuiltinMethod(classId, "__repr__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString("'" + instance.toString() + "'");
        }
    });

    private static final Reference __str__ = new Reference(new BuiltinMethod(classId, "__str__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString(instance.toString());
        }
    });

    private static final Reference __getitem__ = new Reference(new BuiltinMethod(classId, "__getitem__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            int i = args[0].toInteger();
            return new HalString(instance.toString().substring(i, i+1));
        }
    });
}

package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

public class HalBoolean extends HalObject<Boolean>
{
    private static final String classId = "Boolean";

    public HalBoolean(Boolean b)
    {
        super(b);
    }

    public String getClassId() {
        return classId;
    }

    public ReferenceRecord createRecord() {
        ReferenceRecord record = super.createRecord();

        record.defineBuiltin(__str__);
        record.defineBuiltin(__bool__);
        record.defineBuiltin(__not__);

        return record;
    }

    public Boolean toBoolean() {
        return value;
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod(classId, "__str__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString(instance.toBoolean() ? "true" : "false");
        }
    });

    private static final Reference __bool__ = new Reference(new BuiltinMethod(classId, "__bool__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalBoolean(instance.toBoolean());
        }
    });

    private static final Reference __not__ = new Reference(new BuiltinMethod(classId, "__not__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new TypeException();

            return new HalBoolean(!instance.toBoolean());
        }
    });
}

package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;

public class HalNone extends HalObject
{
    public static final String classId = "None";

    public HalNone()
    {
        value = null;
    }

    public String getClassId() {
        return classId;
    }

    public ReferenceRecord createRecord() {
        ReferenceRecord base = super.createRecord();

        base.defineBuiltin(__str__);

        return base;
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod(classId, "__str__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString("None");
        }
    });
}

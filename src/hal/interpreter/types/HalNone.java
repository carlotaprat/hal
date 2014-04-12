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
    
    private static final Reference __str__ = new Reference(new BuiltinMethod("__str__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString("None");
        }
    });
    
    private static final Reference __eq__ = new Reference(new BuiltinMethod("__eq__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalBoolean(args[0] instanceof HalNone); 
        }
    });
    
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalObject.record,
                __str__,
                __eq__);
    
    public ReferenceRecord getRecord() {
        return record;
    }

}

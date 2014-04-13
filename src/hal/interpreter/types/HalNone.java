package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;

public class HalNone extends HalObject
{
    public static final String classId = "None";

    public HalNone() {
        value = null;
    }

    public HalString str() {
        return new HalString("none");
    }

    public HalBoolean bool() {
        return new HalBoolean(false);
    }
    
    private static final Reference __eq__ = new Reference(new BuiltinMethod("eq") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalBoolean(args[0] instanceof HalNone); 
        }
    });
    
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalObject.record,
                __eq__
    );
    
    public ReferenceRecord getRecord() { return record; }
}

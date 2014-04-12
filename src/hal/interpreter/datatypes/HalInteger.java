package hal.interpreter.datatypes;

import hal.interpreter.DataType;
import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;

public class HalInteger extends DataType<Integer>
{
    private static ReferenceRecord cached_record = null;

    public HalInteger(Integer i)
    {
        super(i);
    }

    public ReferenceRecord createRecord() {
        cached_record = super.createRecord();

        cached_record.defineBuiltin(__str__);

        return cached_record;
    }

    public Integer toInteger() {
        return value;
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod("Integer", "__str__") {
        @Override
        public DataType call(DataType instance, DataType... args) {
            return new HalString(Integer.toString(((HalInteger) instance).value));
        }
    });
}

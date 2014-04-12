package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

public class HalInteger extends HalObject<Integer>
{
    private static final String classId = "Integer";

    public HalInteger(Integer i)
    {
        super(i);
    }

    public String getClassId() {
        return classId;
    }

    public ReferenceRecord createRecord() {
        ReferenceRecord record = super.createRecord();

        // Conversion
        record.defineBuiltin(__str__);
        record.defineBuiltin(__bool__);

        // Unary
        record.defineBuiltin(__neg__);

        // Binary
        record.defineBuiltin(__add__);
        record.defineBuiltin(__sub__);

        // Relational
        record.defineBuiltin(__lt__);

        return record;
    }

    public Integer toInteger() {
        return value;
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod(classId, "__str__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalString(Integer.toString(instance.toInteger()));
        }
    });

    private static final Reference __bool__ = new Reference(new BuiltinMethod(classId, "__bool__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new TypeException();

            return new HalBoolean(instance.toInteger() != 0);
        }
    });

    private static final Reference __neg__ = new Reference(new BuiltinMethod(classId, "__neq__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return null;
        }
    });

    private static final Reference __add__ = new Reference(new BuiltinMethod(classId, "__add__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalInteger(instance.toInteger() + args[0].toInteger());
        }
    });

    private static final Reference __sub__ = new Reference(new BuiltinMethod(classId, "__sub__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalInteger(instance.toInteger() - args[0].toInteger());
        }
    });

    private static final Reference __lt__ = new Reference(new BuiltinMethod(classId, "__lt__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalBoolean(instance.toInteger() < args[0].toInteger());
        }
    });
}

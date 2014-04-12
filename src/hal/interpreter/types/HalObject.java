package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;

import java.util.HashMap;


public abstract class HalObject<T> {
    private static final String classId = "Object";
    static HashMap<String, ReferenceRecord> records = new HashMap<String, ReferenceRecord>();

    public T value;
    protected ReferenceRecord record;

    public HalObject() {
        record = getRecord(getClassId());
    }

    /**
     * Standard constructor *
     */
    public HalObject(T d) {
        this();
        value = d;
    }

    public String getClassId() {
        return classId;
    }

    public static void init() {
        // Close circular dependency with BuiltinMethods
        ReferenceRecord builtinRecord = records.get(BuiltinMethod.classId);
        builtinRecord.setRecord(createStaticRecord());
    }

    public T getValue() {
        return value;
    }

    /**
     * Returns a string representing the data in textual form.
     */
    public String toString() {
        return (String) methodcall("__str__").getValue();
    }

    public Integer toInteger() throws TypeException {
        return (Integer) methodcall("__int__").getValue();
    }

    public Boolean toBoolean() throws TypeException {
        return (Boolean) methodcall("__bool__").getValue();
    }

    /**
     * Internal method to handle funcalls
     */
    public HalObject call(HalObject instance, HalObject... args) {
        if (args.length > 0)
            throw new TypeException("No arguments expected");

        return this;
    }

    public HalObject methodcall(String name, HalObject... args) {
        try {
            return record.getVariable(name).call(this, args);
        } catch(NameException e) {
            throw new TypeException(e.getMessage() + " in class " + getClassId());
        }
    }

    private ReferenceRecord getRecord(String id) {
        if(records.containsKey(id))
            return records.get(id);

        ReferenceRecord r = createRecord();
        records.put(id, r);
        return r;
    }

    protected ReferenceRecord createRecord() {
        return createStaticRecord();
    }

    private static ReferenceRecord createStaticRecord() {
        ReferenceRecord base = new ReferenceRecord();

        // Builtin base methods
        base.defineBuiltin(__repr__);
        base.defineBuiltin(__eq__);
        base.defineBuiltin(__neq__);

        return base;
    }

    private static Reference __repr__ = new Reference(new BuiltinMethod(classId, "__repr__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return instance.methodcall("__str__");
        }
    });

    private static Reference __eq__ = new Reference(new BuiltinMethod(classId, "__eq__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalBoolean(instance.getValue().equals(args[0].getValue()));
        }
    });

    private static Reference __neq__ = new Reference(new BuiltinMethod(classId, "__neq__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return __eq__.data.call(instance, args).methodcall("__not__");
        }
    });
}

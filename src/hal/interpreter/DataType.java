package hal.interpreter;

import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

import java.util.HashMap;


public abstract class DataType<T> {
    protected T value;
    static HashMap<Class, ReferenceRecord> records = new HashMap<Class, ReferenceRecord>();

    protected ReferenceRecord record;

    public DataType() {
        record = getRecord();
    }

    /**
     * Standard constructor *
     */
    public DataType(T d) {
        this();
        value = d;
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
    public DataType call(DataType instance, DataType... args) {
        if (args.length > 0)
            throw new TypeException("No arguments expected");

        return this;
    }

    public DataType methodcall(String name, DataType... args) {
        return record.getVariable(name).call(this, args);
    }

    private ReferenceRecord getRecord() {
        Class c = getClass();

        if(records.containsKey(c))
            return records.get(c);

        ReferenceRecord r = createRecord();
        records.put(c, r);
        return r;
    }

    protected ReferenceRecord createRecord() {
        ReferenceRecord base = new ReferenceRecord();

        // Builtin base methods
        base.defineBuiltin(__repr__);

        return base;
    }

    private static Reference __repr__ = new Reference(new BuiltinMethod("Object", "__repr__") {
        @Override
        public DataType call(DataType instance, DataType... args) {
            return instance.methodcall("__str__");
        }
    });
}

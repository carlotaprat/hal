package hal.interpreter.types;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.enumerable.HalString;


public abstract class HalObject<T> extends HalType {
    public static final String classId = "Object";

    public T value;

    public HalObject() {}

    public HalObject(T d) {
        this();
        value = d;
    }

    public T getValue() {
        return value;
    }

    public String toString() {
        return (String) methodcall("__str__").getValue();
    }

    public Boolean toBoolean(){
        return (Boolean) methodcall("__bool__").getValue();
    }

    public HalString repr() {
        return (HalString) methodcall("__str__");
    }

    public HalBoolean not() {
        return new HalBoolean(!((HalBoolean) methodcall("__bool__")).value);
    }

    public HalObject call(HalObject instance, HalObject... args) {
        if (args.length > 0)
            throw new TypeException("No arguments expected");

        return this;
    }

    public HalObject methodcall(String name, HalObject... args) {
        ReferenceRecord original = getRecord();
        ReferenceRecord current = original;

        while(true) {
            try {
                return current.getVariable(name).call(this, args);
            } catch (NameException e) {
                current = current.parent;

                if(current == null)
                    throw new TypeException(e.getMessage() + " in class " + original.name);
            }
        }
    }

    public static final ReferenceRecord record = new ReferenceRecord(classId, HalType.record);
    public ReferenceRecord getRecord() {
        return record;
    }
}

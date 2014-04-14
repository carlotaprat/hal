package hal.interpreter.types;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.AbstractClassException;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.enumerable.HalString;


public abstract class HalObject<T> extends HalType
{
    public static final HalClass klass = new HalClass("Object") {
        public ReferenceRecord getInstanceRecord() { return HalObject.record; }
    };

    public T value;

    public HalObject() {}

    public HalObject(T d) {
        this();
        value = d;
    }

    public T getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if(o instanceof HalObject)
            return methodcall("__eq__", (HalObject) o).toBoolean();

        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    public String toString() {
        return ((HalString) methodcall("__str__")).getValue();
    }

    public Boolean toBoolean(){
        return ((HalBoolean) methodcall("__bool__")).getValue();
    }

    public HalString repr() {
        return (HalString) methodcall("__str__");
    }

    public HalBoolean not() {
        return new HalBoolean(!((HalBoolean) methodcall("__bool__")).value);
    }

    public HalObject call(HalObject instance, HalObject... args) {
        if (args.length > 0)
            throw new InvalidArgumentsException();

        return this;
    }

    public HalObject methodcall(String name, HalObject... args) {
        ReferenceRecord original = getRecord();

        try {
            return original.getVariable(name).call(this, args);
        } catch (NameException e) {
            throw new TypeException(e.getMessage() + " in class " + original.name);
        } catch(InvalidArgumentsException e) {
                throw new TypeException(e.getMessage() + " for " + original.name + "#" + name);
        } catch(AbstractClassException e) {
                throw new TypeException(e.getMessage());
        }
    }

    public static final ReferenceRecord record = new ReferenceRecord(klass.value, HalType.record);
    public ReferenceRecord getRecord() { return record; }
}

package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NewNotSupportedException;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.enumerable.HalString;


public abstract class HalObject<T> extends HalType
{
    public static final HalClass klass = new HalClass("Object", HalType.klass, new Reference[0],
            new Reference(new Builtin("new") {
                @Override
                public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                    HalObject inst = ((HalClass)instance).newInstance((HalClass)instance);
                    inst.methodcall_lambda("init", lambda, args);
                    return inst;
                }
            }))
    {
        public HalObject newInstance(HalClass instklass) {
            return new HalInstance(instklass);
        }
    };

    public T value;
    private ReferenceRecord obj_record;

    public HalObject() {
        initRecord();
    }

    public HalObject(T d) {
        this();
        value = d;
    }

    protected void initRecord() {
        obj_record = new ReferenceRecord(getKlass().getInstanceRecord());
    }

    public T getValue() {
        return value;
    }

    public final ReferenceRecord getRecord() { return obj_record; }

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

    public HalString str() {
        return new HalString("<" + getKlass().value + " @" + System.identityHashCode(this) + ">");
    }

    public HalObject call(HalObject instance, HalMethod lambda, HalObject...args) {
        return call(instance, lambda, new Arguments(args));
    }

    public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
        if (!args.isEmpty() || lambda != null)
            throw new TypeException(getKlass().value + " type is not callable");

        return this;
    }

    public HalObject methodcall(String name, HalObject... args) {
        return methodcall_lambda(name, null, new Arguments(args));
    }

    public HalObject methodcall(String name, Arguments args) {
        return methodcall_lambda(name, null, args);
    }

    public HalObject methodcall_lambda(String name, HalMethod lambda) {
        return methodcall_lambda(name, lambda, new Arguments());
    }

    public HalObject methodcall_lambda(String name, HalMethod lambda, Arguments args) {
        ReferenceRecord original = getRecord();

        try {
            return original.getVariable(name).call(this, lambda, args);
        } catch (NameException e) {
            throw new TypeException(e.getMessage() + " in " + toString() + " of class " + getKlass().value);
        } catch(InvalidArgumentsException e) {
                throw new TypeException(e.getMessage() + " for " + getKlass().value + "#" + name);
        } catch(NewNotSupportedException e) {
                throw new TypeException(e.getMessage() + " in " + toString());
        }
    }
}

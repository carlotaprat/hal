package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.exceptions.TypeException;


public abstract class HalObject<T> {
    public T value;

    public static final String classId = "Object";
    
    public HalObject() {

    }

    /**
     * Standard constructor *
     */
    public HalObject(T d) {
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

    private static Reference __repr__ = new Reference(new BuiltinMethod("repr") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return instance.methodcall("__str__");
        }
    });

    private static Reference __eq__ = new Reference(new BuiltinMethod("eq") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalBoolean(instance.getValue().equals(args[0].getValue()));
        }
    });

    private static Reference __neq__ = new Reference(new BuiltinMethod("neq") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return instance.methodcall("__eq__", args).methodcall("__not__");
        }
    });

    private static final Reference __le__ = new Reference(new BuiltinMethod("le") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalBoolean(instance.methodcall("__lt__", args).toBoolean() ||
                instance.methodcall("__eq__", args).toBoolean());
        }
    });

    private static final Reference __gt__ = new Reference(new BuiltinMethod("gt") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return instance.methodcall("__le__", args).methodcall("__not__");
        }
    });

    private static final Reference __ge__ = new Reference(new BuiltinMethod("ge") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return instance.methodcall("__lt__", args).methodcall("__not__");
        }
    });

    public static final ReferenceRecord record = new ReferenceRecord(classId, null,
            __repr__,
            __eq__,
            __neq__,
            __le__,
            __gt__,
            __ge__
    );


    public ReferenceRecord getRecord() {
        return record;
    }
}

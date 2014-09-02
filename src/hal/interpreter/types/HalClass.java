package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NewNotSupportedException;
import hal.interpreter.types.enumerable.HalString;
import hal.interpreter.types.numeric.HalInteger;

public class HalClass extends HalObject<String>
{
    public static final HalClass klass = new HalClass("Class", null) {
        public void initRecord() {}
    };

    protected HalClass parent;
    private ReferenceRecord instRecord;

    public HalClass(String name, HalClass parent, Reference... builtins) {
        this(name, parent, builtins, new Reference[0]);
    }

    public HalClass(String name, HalClass parent, Reference[] instance, Reference... statik) {
        value = name;
        this.parent = parent;
        instRecord = new ReferenceRecord(null, instance);
        inherit(parent);

        ReferenceRecord statRecord = getRecord();
        for(Reference statMethod : statik)
            statRecord.defineBuiltin(statMethod);
    }

    private void inherit(HalClass parent) {
        if(parent != null) {
            instRecord.parent = parent.getInstanceRecord();
            getRecord().parent = parent.getRecord();
        }
    }

    public HalObject newInstance(HalClass instklass) {
        throw new NewNotSupportedException();
    }

    public HalString str() {
        return new HalString(value);
    }

    public HalBoolean bool() { return new HalBoolean(true); }

    public void solveDependency() {
        super.initRecord();
        inherit(HalObject.klass);
        HalMethod.klass.inherit(HalObject.klass);
        HalMethod.klass.getInstanceRecord().defineBuiltin(new Reference(new Builtin("arity") {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                HalInteger arity = new HalInteger(((HalMethod)instance).value.getArity());
                instance.getRecord().defineVariable("arity", arity);
                return arity;
            }
        }));
        HalMethod.klass.getInstanceRecord().defineBuiltin(new Reference(new Builtin("break?") {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                return new HalBoolean(((HalMethod) instance).isBreakRequested());
            }
        }));
    }

    public ReferenceRecord getInstanceRecord() { return instRecord; }
    public HalClass getKlass(){ return HalClass.klass; }
}

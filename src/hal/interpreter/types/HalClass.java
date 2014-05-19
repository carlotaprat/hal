package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;

public class HalClass extends HalObject<String>
{
    public static final HalClass klass = new HalClass("Class", null) {
        public void initRecord() {}
    };

    private ReferenceRecord instRecord;

    public HalClass(String name, HalClass parent, Reference... builtins) {
        value = name;
        ReferenceRecord inherit = (parent == null) ? null : parent.getInstanceRecord();
        instRecord = new ReferenceRecord(inherit, builtins);
    }

    public HalObject _new(Arguments args) {
        HalObject instance = new HalInstance(this);
        instance.methodcall("init", args);

        return instance;
    }

    public HalString str() {
        return new HalString("<Class: " + value + ">");
    }

    public HalBoolean bool() { return new HalBoolean(true); }

    public void solveDependency() {
        super.initRecord();
        instRecord.parent = HalObject.klass.getInstanceRecord();

        Reference[] builtins = new Reference[] {
            new Reference(new Builtin("new") {
                @Override
                public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                    return ((HalClass)instance)._new(args);
                }
            })
        };

        for(Reference builtin : builtins)
            instRecord.defineBuiltin(builtin);
    }

    public ReferenceRecord getInstanceRecord() { return instRecord; }
    public HalClass getKlass(){ return HalClass.klass; }
}

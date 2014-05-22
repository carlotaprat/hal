package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NewNotSupportedException;
import hal.interpreter.types.enumerable.HalString;

public class HalClass extends HalObject<String>
{
    public static final HalClass klass = new HalClass("Class", null) {
        public void initRecord() {}
    };

    protected HalClass parent;
    private ReferenceRecord instRecord;

    public HalClass(String name, HalClass parent, Reference... builtins) {
        value = name;
        this.parent = parent;
        instRecord = new ReferenceRecord(null, builtins);
        inherit(parent);
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

        HalObject.klass.getRecord().defineBuiltin(new Reference(new Builtin("new") {
            @Override
            public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                HalObject inst = ((HalClass)instance).newInstance((HalClass)instance);
                inst.methodcall_lambda("init", lambda, args);
                return inst;
            }
        }));
    }

    public ReferenceRecord getInstanceRecord() { return instRecord; }
    public HalClass getKlass(){ return HalClass.klass; }
}

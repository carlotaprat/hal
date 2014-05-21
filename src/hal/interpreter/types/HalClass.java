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

    private HalClass parent;
    private ReferenceRecord instRecord;

    public HalClass(String name, HalClass parent, Reference... builtins) {
        this(name, true, parent, builtins);
    }

    public HalClass(String name, boolean abstrakt, HalClass parent, Reference... builtins) {
        value = name;
        this.parent = parent;
        ReferenceRecord inherit = (parent == null) ? null : parent.getInstanceRecord();
        instRecord = new ReferenceRecord(inherit, builtins);

        if(!abstrakt)
            getRecord().defineBuiltin(new Reference(new Builtin("new") {
                @Override
                public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                    HalObject inst = ((HalClass)instance).newInstance((HalClass)instance);
                    inst.methodcall_lambda("init", lambda, args);
                    return inst;
                }
            }));
    }

    public HalObject newInstance(HalClass instklass) {
        return parent.newInstance(instklass);
    }

    public HalString str() {
        return new HalString("<Class: " + value + ">");
    }

    public HalBoolean bool() { return new HalBoolean(true); }

    public void solveDependency() {
        super.initRecord();
        instRecord.parent = HalObject.klass.getInstanceRecord();
    }

    public ReferenceRecord getInstanceRecord() { return instRecord; }
    public HalClass getKlass(){ return HalClass.klass; }
}

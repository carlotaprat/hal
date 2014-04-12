package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

import java.util.ArrayList;
import java.util.List;


public class HalArray extends HalObject<List<HalObject>>
{
    private static final String classId = "Array";

    public HalArray() {
        value = new ArrayList<HalObject>();
    }

    public String getClassId() {
        return classId;
    }

    protected ReferenceRecord createRecord() {
        ReferenceRecord record = super.createRecord();

        record.defineBuiltin(__str__);
        record.defineBuiltin(__getitem__);
        record.defineBuiltin(__setitem__);
        record.defineBuiltin(append);
        record.defineBuiltin(size);
        record.defineBuiltin(sum);

        return record;
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod(classId, "__str__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            String s = "";
            boolean first = true;
            HalArray i = (HalArray) instance;

            for(HalObject element : i.value) {
                if(first) first = false;
                else s += ", ";

                s += element.methodcall("__repr__").getValue();
            }

            return new HalString("[" + s + "]");
        }
    });

    private static final Reference __getitem__ = new Reference(new BuiltinMethod(classId, "__getitem__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return ((HalArray) instance).value.get(args[0].toInteger());
        }
    });

    private static final Reference __setitem__ = new Reference(new BuiltinMethod(classId, "__setitem__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 2)
                throw new TypeException();

            ((HalArray) instance).value.set(args[0].toInteger(), args[1]);
            return args[1];
        }
    });

    private static final Reference append = new Reference(new BuiltinMethod(classId, "append") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            ((HalArray) instance).value.add(args[0]);
            return instance;
        }
    });

    private static final Reference size = new Reference(new BuiltinMethod(classId, "size") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalInteger(((HalArray) instance).value.size());
        }
    });

    private static final Reference sum = new Reference(new BuiltinMethod(classId, "sum") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            HalObject sum = new HalInteger(0);

            HalArray i = (HalArray) instance;
            for(HalObject element : i.value)
                sum = sum.methodcall("__add__", element);

            return sum;
        }
    });
}

package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

import hal.interpreter.types.numeric.HalInteger;
import java.util.ArrayList;
import java.util.List;


public class HalArray extends HalObject<List<HalObject>>
{
    private static final String classId = "Array";

    public HalArray() {
        value = new ArrayList<HalObject>();
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod("__str__") {
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

    private static final Reference __getitem__ = new Reference(new BuiltinMethod("__getitem__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return ((HalArray) instance).value.get(((HalInteger)args[0]).toInteger());
        }
    });

    private static final Reference __setitem__ = new Reference(new BuiltinMethod("__setitem__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 2)
                throw new TypeException();

            ((HalArray) instance).value.set(((HalInteger)args[0]).toInteger(), args[1]);
            return args[1];
        }
    });

    private static final Reference append = new Reference(new BuiltinMethod("append") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            ((HalArray) instance).value.add(args[0]);
            return instance;
        }
    });

    private static final Reference size = new Reference(new BuiltinMethod("size") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            return new HalInteger(((HalArray) instance).value.size());
        }
    });

    private static final Reference sum = new Reference(new BuiltinMethod("sum") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            HalObject sum = new HalInteger(0);

            HalArray i = (HalArray) instance;
            for(HalObject element : i.value)
                sum = sum.methodcall("__add__", element);

            return sum;
        }
    });
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalObject.record,
            __str__,
            __getitem__,
            __setitem__,
            append,
            size,
            sum
    );
    
    public ReferenceRecord getRecord() {
        return record;
    }
}

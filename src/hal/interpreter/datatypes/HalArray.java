package hal.interpreter.datatypes;

import hal.interpreter.DataType;
import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;

import java.util.ArrayList;
import java.util.List;


public class HalArray extends DataType<List<DataType>>
{
    public HalArray() {
        value = new ArrayList<DataType>();
    }

    protected ReferenceRecord createRecord() {
        ReferenceRecord record = super.createRecord();

        record.defineBuiltin(__str__);
        record.defineBuiltin(__getitem__);

        return record;
    }

    public DataType __getitem__(DataType index) {
        return value.get(index.toInteger());
    }

    public DataType __setitem__(DataType index, DataType item) {
        value.set(index.toInteger(), item);
        return item;
    }

    public DataType __append__(DataType item) {
        value.add(item);
        return this;
    }

    public DataType __size__() {
        return new HalInteger(value.size());
    }

    public DataType __sum__() {
        // Optimized, we avoid to use AslIntegers here
        // to be fast!
        int sum = 0;

        for(DataType element : value)
            sum += element.toInteger();

        return new HalInteger(sum);
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod("Array", "__str__") {
        @Override
        public DataType call(DataType instance, DataType... args) {
            String s = "";

            boolean first = true;
            HalArray i = (HalArray) instance;
            for(DataType element : i.value) {
                if(first) first = false;
                else s += ", ";

                s += element.methodcall("__repr__").getValue();
            }

            return new HalString("[" + s + "]");
        }
    });

    private static final Reference __getitem__ = new Reference(new BuiltinMethod("Array", "__getitem__") {
        @Override
        public DataType call(DataType instance, DataType... args) {
            if(args.length != 1)
                throw new TypeException();

            return ((HalArray) instance).value.get(args[0].toInteger());
        }
    });
}

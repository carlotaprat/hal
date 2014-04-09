package hal.interpreter.datatypes;

import hal.interpreter.DataType;
import java.util.ArrayList;
import java.util.List;


public class HalArray extends DataType<List<DataType>>
{
    public HalArray() {
        value = new ArrayList<DataType>();
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

    public HalString __str__() {
        String s = "";

        boolean first = true;
        for(DataType element : value) {
            if(first) first = false;
            else s += ", ";

            s += element.__repr__().getValue();
        }

        return new HalString("[" + s + "]");
    }
}

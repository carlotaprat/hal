package hal.interpreter.types.enumerable;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.KeyException;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;

import java.util.HashMap;
import java.util.Map;


public class HalDictionary extends HalEnumerable<HashMap<String, HalObject>>
{
    public HalDictionary() {
        value = new HashMap<String, HalObject>();
    }

    public HalString str() {
        String s = "";
        boolean first = true;

        for(Map.Entry<String, HalObject> e : value.entrySet()) {
            if(first) first = false;
            else s += ", ";

            HalString key = new HalString(e.getKey());
            s += key.methodcall("__repr__") + " => " + e.getValue().methodcall("__repr__");
        }

        return new HalString("{" + s + "}");
    }

    public HalObject getitem(HalInteger index) {
        HalObject item = value.get(index.toString());

        if(item == null)
            throw new KeyException("Invalid key " + index.methodcall("__str__").methodcall("__repr__"));

        return item;
    }

    public void setitem(HalInteger index, HalObject item) {
        value.put(index.toString(), item);
    }

    public HalInteger size() {
        return new HalInteger(value.size());
    }

    private static final ReferenceRecord record = new ReferenceRecord("HalDictionary", HalEnumerable.record);
    public ReferenceRecord getRecord() { return record; }
}

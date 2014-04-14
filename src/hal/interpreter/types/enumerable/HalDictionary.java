package hal.interpreter.types.enumerable;

import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.KeyException;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;

import java.util.HashMap;
import java.util.Map;


public class HalDictionary extends HalEnumerable<HashMap<HalObject, HalObject>>
{
    public static final HalClass klass = new HalClass("Dictionary") {
        public ReferenceRecord getInstanceRecord() { return HalDictionary.record; }
    };
    public HalClass getKlass() { return klass; }

    public HalDictionary() {
        value = new HashMap<HalObject, HalObject>();
    }

    public HalString str() {
        String s = "";
        boolean first = true;

        for(Map.Entry<HalObject, HalObject> e : value.entrySet()) {
            if(first) first = false;
            else s += ", ";

            s += e.getKey().methodcall("__repr__") + " => " + e.getValue().methodcall("__repr__");
        }

        return new HalString("{" + s + "}");
    }

    public HalObject getitem(HalObject index) {
        HalObject item = value.get(index);

        if(item == null)
            throw new KeyException("Invalid key " + index.methodcall("__repr__"));

        return item;
    }

    public void setitem(HalObject index, HalObject item) {
        value.put(index, item);
    }

    public HalInteger size() {
        return new HalInteger(value.size());
    }

    private static final ReferenceRecord record = new ReferenceRecord(klass.value, HalEnumerable.record);
    public ReferenceRecord getRecord() { return record; }
}

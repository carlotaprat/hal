package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.KeyException;
import hal.interpreter.exceptions.TypeException;

import java.util.HashMap;
import java.util.Map;


public class HalDictionary extends HalObject<HashMap<String, HalObject>> {
    public HalDictionary() {
        super(new HashMap<String, HalObject>());
    }

    private static final Reference __str__ = new Reference(new BuiltinMethod("str") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length > 0)
                throw new TypeException();

            HalDictionary i = (HalDictionary) instance;
            String s = "";
            boolean first = true;

            for(Map.Entry<String, HalObject> e : i.value.entrySet()) {
                if(first) first = false;
                else s += ", ";

                HalString key = new HalString(e.getKey());
                s += key.methodcall("__repr__") + " => " + e.getValue().methodcall("__repr__");
            }

            return new HalString("{" + s + "}");
        }
    });

    private static final Reference __getitem__ = new Reference(new BuiltinMethod("getitem") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            HalObject item = ((HalDictionary) instance).value.get(args[0].toString());

            if(item == null)
                throw new KeyException("Invalid key " + args[0].methodcall("__str__").methodcall("__repr__"));

            return item;
        }
    });

    private static final Reference __setitem__ = new Reference(new BuiltinMethod("setitem") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 2)
                throw new TypeException();

            ((HalDictionary) instance).value.put(args[0].toString(), args[1]);
            return args[1];
        }
    });

    private static final ReferenceRecord record = new ReferenceRecord("HalDictionary", HalObject.record,
            __str__,
            __getitem__,
            __setitem__
    );

    public ReferenceRecord getRecord() { return record; }
}

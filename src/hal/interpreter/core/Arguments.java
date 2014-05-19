package hal.interpreter.core;


import hal.interpreter.types.HalObject;

import java.util.HashMap;
import java.util.Set;

public class Arguments {
    public HalObject[] pos;
    public HashMap<String, HalObject> args;

    public Arguments() {
        pos = new HalObject[0];
        args = new HashMap<String, HalObject>();
    }

    public Arguments(HalObject...pos) {
        this();
        this.pos = pos;
    }

    public Arguments(HalObject[] pos, Params.Keyword...keywords) {
        this(pos);

        for(Params.Keyword kw : keywords)
            args.put(kw.name, kw.value);
    }

    public Arguments(Params.Keyword...keywords) {
        this(null, keywords);
    }

    public boolean isEmpty(){
        return pos.length == 0 && args.size() == 0;
    }

    public HalObject get(String name) {
        return args.get(name);
    }

    public void put(String name, HalObject arg) {
        args.put(name, arg);
    }

    public boolean contains(String name) {
        return args.containsKey(name);
    }

    public int size() {
        return args.size();
    }

    public Set<String> keys() {
        return args.keySet();
    }
}

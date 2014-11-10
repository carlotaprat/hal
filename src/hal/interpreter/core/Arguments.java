package hal.interpreter.core;


import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalArray;

import java.util.*;

public class Arguments {
    public ArrayList<HalObject> pos;
    public HashMap<String, HalObject> args;

    public Arguments() {
        pos = new ArrayList<HalObject>();
        args = new HashMap<String, HalObject>();
    }

    public Arguments(HalObject...pos) {
        this();
        this.pos = new ArrayList<HalObject>(Arrays.asList(pos));
    }

    public Arguments(HalObject[] pos, Params.Keyword...keywords) {
        this(pos);

        for(Params.Keyword kw : keywords)
            args.put(kw.name, kw.value);
    }

    public Arguments(HalArray array) {
        this();
        pos.addAll(array.value);
    }

    public Arguments(Params.Keyword...keywords) {
        this(null, keywords);
    }

    public void prepend(HalObject arg) {
        pos.add(0, arg);
    }

    public void append(HalObject arg) {
        pos.add(arg);
    }

    public boolean isEmpty(){
        return pos.size() == 0 && args.size() == 0;
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

    public Set<Map.Entry<String, HalObject>> entrySet() {
        return args.entrySet();
    }
}

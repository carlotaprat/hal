package hal.interpreter.core;

import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.SyntaxException;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalArray;

import java.util.ArrayList;
import java.util.HashMap;


public class Params {
    public ArrayList<String> positional;
    public HashMap<String, HalObject> keywords;
    public String group_params;
    public int before_group;
    public int after_group;

    static public class Param {
        public String name;

        public Param(String name){
            this.name = name;
        }
    }

    static public class Group extends Param {
        public Group(String name) {
            super(name);
        }
    }

    static public class Keyword extends Param {
        public HalObject value;

        public Keyword(String name, HalObject value) {
            super(name);
            this.value = value;
        }
    }

    public Params(Param...params) {
        positional = new ArrayList<String>();
        keywords = new HashMap<String, HalObject>();
        group_params = null;
        before_group = 0;
        after_group = 0;

        for (Param param : params) {
            if (param instanceof Group) {
                if (group_params != null)
                    throw new SyntaxException("More than one param group not allowed.");

                group_params = param.name;
            } else {
                positional.add(param.name);

                if (group_params == null)
                    before_group++;
                else
                    after_group++;

                if(param instanceof Keyword) {
                    Keyword kw = (Keyword) param;
                    keywords.put(kw.name, kw.value);
                }
            }
        }
    }

    public Arguments fill(Arguments args) {
        int num_pos_params = positional.size();
        int num_pos_args = args.pos.length;
        int total = 0;

        // Copy the parameters to the record activation record
        for(int i = 0; i < before_group && i < num_pos_args; ++i) {
            args.put(positional.get(i), args.pos[i]);
            total++;
        }

        int before = total;

        for(int i = 0; i < after_group && i < (num_pos_args-before); ++i) {
            args.put(positional.get(num_pos_params - i - 1), args.pos[args.pos.length - i - 1]);
            total++;
        }

        if(group_params != null && !args.contains(group_params)) {
            HalArray group = new HalArray();

            int last_group_param = args.pos.length - total + before_group;
            for(int i = before_group; i < last_group_param; ++i)
                group.methodcall("__append!__", args.pos[i]);

            args.put(group_params, group);
        }

        for(String keyword : keywords.keySet()) {
            if(!args.contains(keyword))
                args.put(keyword, keywords.get(keyword));
        }

        if(args.size() != size())
            throw new InvalidArgumentsException();

        return args;
    }

    public int size() {
        int size = positional.size();

        if(group_params == null)
            return size;

        return size+1;
    }
}

package hal.interpreter.core;

import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.exceptions.SyntaxException;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalDictionary;
import hal.interpreter.types.enumerable.HalString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Params {
    public ArrayList<String> positional;
    public HashMap<String, Boolean> signature;
    public HashMap<String, HalObject> keywords;
    public String group_params;
    public String group_keywords;
    public int before_group;
    public int after_group;
    public int unpositional_keywords;

    static public class Param {
        enum TYPE { POSITIONAL, PARAM_GROUP, KEYWORD, KEYWORD_GROUP }

        public String name;
        public TYPE type;

        public Param(String name, TYPE type){
            this.name = name;
            this.type = type;
        }

        public Param(String name) {
            this(name, TYPE.POSITIONAL);
        }

        public TYPE getType() { return type; }
    }

    static public class ParamGroup extends Param {
        public ParamGroup(String name) {
            super(name, TYPE.PARAM_GROUP);
        }
    }

    static public class Keyword extends Param {
        public HalObject value;

        public Keyword(String name, HalObject value) {
            super(name, TYPE.KEYWORD);
            this.value = value;
        }
    }

    static public class KeywordGroup extends Param {
        public KeywordGroup(String name) {
            super(name, TYPE.KEYWORD_GROUP);
        }
    }

    public Params(Param...params) {
        signature = new HashMap<String, Boolean>();
        positional = new ArrayList<String>();
        keywords = new HashMap<String, HalObject>();
        group_params = null;
        group_keywords = null;
        before_group = 0;
        after_group = 0;
        unpositional_keywords = 0;

        for (Param param : params) {
            switch(param.getType()) {
                case POSITIONAL:
                    positional.add(param.name);

                    if(group_params == null)
                        before_group++;
                    else
                        after_group++;
                    break;

                case KEYWORD:
                    Keyword kw = (Keyword) param;
                    keywords.put(kw.name, kw.value);

                    if(group_params == null) {
                        positional.add(param.name);
                        before_group++;
                    } else {
                        unpositional_keywords++;
                    }
                    break;

                case PARAM_GROUP:
                    if (group_params != null)
                        throw new SyntaxException("More than one param group not allowed.");

                    group_params = param.name;
                    break;

                case KEYWORD_GROUP:
                    group_keywords = param.name;
                    unpositional_keywords++;
                    break;
            }

            signature.put(param.name, true);
        }
    }

    public Arguments fill(Arguments args) {
        int num_pos_params = positional.size();
        int num_pos_args = args.pos.size();
        int total = 0;

        if(group_params == null && num_pos_args > positional.size())
            throw new InvalidArgumentsException();

        if(group_keywords != null) {
            HalDictionary kwargs = new HalDictionary();

            Iterator<Map.Entry<String, HalObject>> it = args.entrySet().iterator();
            while (it.hasNext())
            {
                Map.Entry<String, HalObject> item = it.next();
                String key = item.getKey();

                if(! signature.containsKey(key)) {
                    kwargs.setitem(new HalString(key), item.getValue());
                    it.remove();
                }
            }

            args.put(group_keywords, kwargs);
        }

        for(int i = 0; i < before_group && i < num_pos_args; ++i) {
            args.put(positional.get(i), args.pos.get(i));
            total++;
        }

        int before = total;

        for(int i = 0; i < after_group && i < (num_pos_args-before); ++i) {
            args.put(positional.get(num_pos_params - i - 1), args.pos.get(num_pos_args - i - 1));
            total++;
        }

        if(group_params != null && !args.contains(group_params)) {
            HalArray group = new HalArray();

            int last_group_param = num_pos_args - total + before_group;
            for(int i = before_group; i < last_group_param; ++i)
                group.methodcall("__append!__", args.pos.get(i));

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
        int size = positional.size() + unpositional_keywords;

        if(group_params == null)
            return size;

        return size+1;
    }
}

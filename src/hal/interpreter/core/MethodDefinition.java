package hal.interpreter.core;

import hal.interpreter.HalTree;
import hal.interpreter.exceptions.SyntaxException;
import hal.interpreter.types.HalModule;
import hal.parser.HalLexer;

import java.util.ArrayList;


public class MethodDefinition
{
    public HalModule module;
    public String name;
    public HalTree tree;
    public HalTree block;

    public class Params {
        public ArrayList<String> positional_params;
        public String group_params;
        public int before_group;
        public int after_group;

        public Params(HalTree params) {
            positional_params = new ArrayList<String>();
            group_params = null;
            before_group = 0;
            after_group = 0;

            int nparams = params.getChildCount();

            for(int i = 0; i < nparams; ++i) {
                HalTree param = params.getChild(i);

                if(param.getType() == HalLexer.PARAM_GROUP) {
                    if(group_params != null)
                        throw new SyntaxException("More than one param group not allowed.");

                    group_params = param.getChild(0).getText();
                } else {
                    positional_params.add(param.getText());

                    if(group_params == null)
                        before_group++;
                    else
                        after_group++;
                }
            }
        }
    }

    public Params params;

    public MethodDefinition() { }

    public MethodDefinition(HalModule mod, HalTree t) {
        module = mod;
        name = t.getChild(0).getText();
        tree = t;
        params = new Params(t.getChild(1));
        block = t.getChild(2);
    }

    public ReferenceRecord getLocals() {
        return new ReferenceRecord(name, null);
    }
}

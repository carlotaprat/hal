package hal.interpreter.core;

import hal.interpreter.HalTree;


public class MethodDefinition
{
    public String name;
    public HalTree tree;
    public HalTree params;
    public HalTree block;

    public MethodDefinition(HalTree t) {
        name = t.getChild(0).getText();
        tree = t;
        params = t.getChild(1);
        block = t.getChild(2);
    }
}

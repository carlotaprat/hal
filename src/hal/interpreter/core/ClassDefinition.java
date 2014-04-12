package hal.interpreter.core;


public class ClassDefinition extends ReferenceRecord
{
    public String name;

    public ClassDefinition(String n) {
        super(n, null);
        name = n;
    }
}

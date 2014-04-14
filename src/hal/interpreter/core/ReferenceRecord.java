package hal.interpreter.core;

import hal.interpreter.exceptions.NameException;
import hal.interpreter.types.HalObject;
import hal.interpreter.Reference;

import java.util.HashMap;


public class ReferenceRecord
{
    public String name;
    public ReferenceRecord parent;
    public HashMap<String, Reference> record;
    
    public ReferenceRecord() {
        name = null;
        parent = null;
        record = null;
    }
    
    public ReferenceRecord(String name, ReferenceRecord parent, Reference... builtins) {
        this.name = name;
        this.parent = parent;
        record = new HashMap<String, Reference>();

        for(Reference builtin : builtins)
            defineBuiltin(builtin);
    }

    public void defineReference(String name, Reference ref) {
        record.put(name, ref);
    }

    /** Defines the value of a variable. If the variable does not
     * exist, it is created. If it exists, the value and type of
     * the variable are re-defined.
     * @param name The name of the variable
     * @param value The value of the variable
     */
    public void defineVariable(String name, HalObject value) {
        Reference r = record.get(name);
        if (r == null) record.put(name, new Reference(value)); // New definition
        else r.data = value; // Use the previous data
    }

    public void defineBuiltin(Reference ref) {
        String name = ref.data.getValue().toString();
        defineReference("__" + name + "__", ref);
        defineReference(name, new Reference(ref.data));
    }

    public Reference getReference(String name) {
        Reference r = record.get(name);
        if (r == null) {
            if(parent != null)
                return parent.getReference(name);

            throw new NameException(name);
        }
        return r;
    }

    /** Gets the value of the variable. The value is represented as
     * a Data object. In this way, any modification of the object
     * implicitly modifies the value of the variable.
     * @param name The name of the variable
     * @return The value of the variable
     */
    public HalObject getVariable(String name) {
        return getReference(name).data;
    }
}

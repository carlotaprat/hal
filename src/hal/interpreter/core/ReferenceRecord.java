package hal.interpreter.core;

import hal.interpreter.Reference;
import hal.interpreter.exceptions.NameException;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalObject;

import java.util.HashMap;


public class ReferenceRecord
{
    public ReferenceRecord parent;
    public HashMap<String, Reference> record;

    public ReferenceRecord() {
        parent = null;
        record = null;
    }

    public ReferenceRecord(ReferenceRecord parent, Reference... builtins) {
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

    public HalObject getUnsafeVariable(String name) {
        Reference r = record.get(name);

        if(r != null)
            return r.data;

        if(parent != null)
            return parent.getUnsafeVariable(name);

        return null;
    }

    public void defineReturn(HalObject obj) {
        Reference r = record.get("return");

        if(r != null) {
            r.data = obj;

            if(parent != null)
                parent.defineReturn(obj);
        }
    }

    public void defineBuiltin(Reference ref) {
        String name = ((HalMethod)ref.data).getValue().name;
        defineReference("__" + name + "__", ref);
        defineReference(name, new Reference(ref.data));
    }

    public void defineMethod(HalMethod method) {
        defineVariable(method.getName(), method);
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

    public boolean hasVariable(String name) {
        return record.containsKey(name);
    }
}

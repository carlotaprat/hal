package hal.interpreter.types;


import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalString;

import java.io.File;

public class HalPackage extends HalObject<String>
{
    public HalPackage parent;
    public String path;
    public String address;
    public HalPackage root;

    public HalPackage(String name, HalPackage par) {
        value = name;
        parent = par;
        path = name;
        address = name;

        if(parent == null)
            root = this;
        else {
            path = parent.path + File.separator + path;
            address = parent.address + '.' + address;
            root = parent.root;
            parent.getInstanceRecord().defineVariable(value, this);
        }
    }

    public String getPath() {
        return path;
    }

    public String getAddress() {
        return address;
    }

    public HalString str() {
        return new HalString("<Package: " + value + ">");
    }

    public HalBoolean bool() {
        return new HalBoolean(true);
    }

    public ReferenceRecord getInstanceRecord() {
        return getRecord();
    }

    public HalClass getKlass() {
        return HalPackage.klass;
    }

    public static final HalClass klass = new HalClass("Package", HalObject.klass);
}

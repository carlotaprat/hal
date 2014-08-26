package hal.interpreter.types;

import hal.interpreter.types.enumerable.HalString;


public class HalModule extends HalPackage
{
    private String fullpath;

    public HalModule(String name, HalPackage parent) {
        super(name, parent);
        this.fullpath = name;
    }

    public HalModule(String name, HalPackage parent, String fullpath) {
        super(name, parent);
        this.fullpath = fullpath;
    }

    public String getFullPath() {
        return fullpath;
    }

    public void setFullPath(String fullpath) {
        this.fullpath = fullpath;
    }

    public String getPath() {
        return path + ".hal";
    }

    public HalString str() {
        return new HalString("<Module: " + value + ">");
    }

    public HalClass getKlass() {
        return HalModule.klass;
    }

    public static final HalClass klass = new HalClass("Module", HalKernel.klass);
}

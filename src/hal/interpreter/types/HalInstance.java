package hal.interpreter.types;


import hal.interpreter.types.enumerable.HalString;

public class HalInstance extends HalObject<HalClass>
{
    private HalClass instklass;

    public HalInstance(HalClass klass) {
        instklass = klass;
        super.initRecord();
    }

    public void initRecord() {
        // instklass is not initialized on construction
        // we need to override this behaviour
        // pretty ugly, I know
    }

    public HalBoolean bool() {
        return new HalBoolean(true);
    }

    public HalClass getKlass() {
        return instklass;
    }
}

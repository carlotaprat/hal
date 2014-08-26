package hal.interpreter.modules;

import hal.interpreter.types.HalModule;
import hal.interpreter.types.HalPackage;
import hal.interpreter.types.numeric.HalInteger;


public class random extends HalModule {
    public random(HalPackage pkg) {
        super("random", pkg);

        getInstanceRecord().defineVariable("magic", new HalInteger(42));
    }
}

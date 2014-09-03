package hal.interpreter.modules;

import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalModule;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.HalPackage;
import hal.interpreter.types.numeric.HalInteger;

import java.util.Random;


public class random extends HalModule {
    private Random generator;

    public random(HalPackage pkg) {
        super("random", pkg);

        generator = new Random(System.currentTimeMillis());
        ReferenceRecord module = getInstanceRecord();

        module.defineMethod(new Builtin("integer") {
            @Override
            public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                return new HalInteger(generator.nextInt());
            }
        });
    }
}

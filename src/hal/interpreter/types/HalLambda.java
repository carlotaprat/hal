package hal.interpreter.types;

import hal.interpreter.core.Lambda;


public class HalLambda extends HalMethod
{
    private HalObject self;

    public HalLambda(Lambda lambda) {
        super(lambda);
        self = lambda.getInstance();
    }

    public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
        return super.call(self, lambda, args);
    }
}

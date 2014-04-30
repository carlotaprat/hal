package hal.interpreter.types;

import hal.interpreter.core.LambdaDefinition;


public class HalLambda extends HalMethod
{
    private HalObject self;

    public HalLambda(LambdaDefinition lambda) {
        super(lambda);
        self = lambda.getInstance();
    }

    public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
        return super.call(self, lambda, args);
    }
}

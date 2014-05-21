package hal.interpreter.types;

import hal.interpreter.HalTree;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.LambdaDefinition;


public class HalLambda extends HalDefinedMethod
{
    private HalObject self;

    public HalLambda(LambdaDefinition lambda, HalTree block) {
        super(lambda, block);
        self = lambda.getInstance();
    }

    public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
        return super.mcall(self, lambda, args);
    }
}

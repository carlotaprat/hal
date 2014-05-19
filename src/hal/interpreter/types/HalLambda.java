package hal.interpreter.types;

import hal.interpreter.HalTree;
import hal.interpreter.core.LambdaDefinition;


public class HalLambda extends HalDefinedMethod
{
    public HalLambda(LambdaDefinition lambda, HalTree block) {
        super(lambda, block);
    }
}

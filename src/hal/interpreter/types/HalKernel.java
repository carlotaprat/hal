package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalDictionary;
import hal.interpreter.types.enumerable.HalString;
import hal.interpreter.types.numeric.HalFloat;
import hal.interpreter.types.numeric.HalInteger;
import hal.interpreter.types.numeric.HalRational;

abstract public class HalKernel<T> extends HalObject<T>
{
    public static void init(){
        // Resolves circular dependency
        HalClass.klass.solveDependency();

        HalClass[] klasses = new HalClass[]{
                // Class
                HalClass.klass,

                // Core objects
                HalObject.klass,
                HalMethod.klass,
                HalNone.klass,

                // Boolean
                HalBoolean.klass,

                // Enumerables
                HalArray.klass,
                HalDictionary.klass,
                HalString.klass,

                // Numerics
                HalInteger.klass,
                HalFloat.klass,
                HalRational.klass

                // TODO: Add klasses of HalNumber and HalEnumerable too!
        };

        for(HalClass klass : klasses)
            record.defineVariable(klass.value, klass);
    }

    private static final Reference __print__ = new Reference(new BuiltinMethod("print") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            if(args.length > 0) {
                for(HalObject arg : args)
                    System.out.println(arg);
            } else {
                System.out.println();
            }

            return HalNone.NONE;
        }
    });

    public static final ReferenceRecord record = new ReferenceRecord("Kernel", HalObject.record,
            __print__
    );
}

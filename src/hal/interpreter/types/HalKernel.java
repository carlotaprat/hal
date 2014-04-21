package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.InvalidArgumentsException;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalDictionary;
import hal.interpreter.types.enumerable.HalEnumerable;
import hal.interpreter.types.enumerable.HalString;
import hal.interpreter.types.numeric.HalFloat;
import hal.interpreter.types.numeric.HalInteger;
import hal.interpreter.types.numeric.HalNumber;
import hal.interpreter.types.numeric.HalRational;

abstract public class HalKernel<T> extends HalObject<T>
{
    public static void init(){
        // Resolve circular dependencies
        HalClass.klass.solveDependency();
        HalMethod.klass.getInstanceRecord().parent = HalObject.klass.getInstanceRecord();

        HalClass[] klasses = new HalClass[]{
                // Class
                HalClass.klass,

                // Core objects
                HalKernel.klass,
                HalObject.klass,
                HalMethod.klass,
                HalNone.klass,
                HalPackage.klass,
                HalModule.klass,

                // Boolean
                HalBoolean.klass,

                // Enumerables
                HalEnumerable.klass,
                HalArray.klass,
                HalDictionary.klass,
                HalString.klass,

                // Numerics
                HalNumber.klass,
                HalInteger.klass,
                HalFloat.klass,
                HalRational.klass
        };

        ReferenceRecord record = HalKernel.klass.getInstanceRecord();
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
    
    private static final Reference range = new Reference(new BuiltinMethod("range") {
        @Override
        public HalObject call(HalObject instance, HalObject lambda, HalObject... args) {
            int ini = 0;
            int end = -1;
            int step = 1;
            
            switch (args.length) {
                case 1:
                    end = ((HalInteger)args[0]).toInteger(); break;
                case 2:
                    ini = ((HalInteger)args[0]).toInteger();
                    end = ((HalInteger)args[1]).toInteger();
                    break;
                case 3:
                    ini = ((HalInteger)args[0]).toInteger();
                    end = ((HalInteger)args[1]).toInteger();
                    step = ((HalInteger)args[2]).toInteger();
                    break;
                default:
                    throw new InvalidArgumentsException();
            }
            HalArray arr = new HalArray();
            for (int i = ini; i < end; i+=step) {
                arr.methodcall("__append!__", new HalInteger(i));
            }
            return arr;
        }
    });

    public static final HalClass klass = new HalClass("Kernel", HalObject.klass,
            __print__,
            range
    );
}

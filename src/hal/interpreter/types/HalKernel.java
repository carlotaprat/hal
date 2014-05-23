package hal.interpreter.types;


import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.NameException;
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
                HalRational.klass,

                // Misc
                HalProcess.klass,
                HalFile.klass
        };

        ReferenceRecord record = HalKernel.klass.getInstanceRecord();
        for(HalClass klass : klasses)
            record.defineVariable(klass.value, klass);
    }

    private static final Reference __print__ = new Reference(new Builtin("print", new Params.ParamGroup("args")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray a = (HalArray) args.get("args");

            if(a.value.size() > 0) {
                for(HalObject arg : a.value)
                    System.out.println(arg);
            } else {
                System.out.println();
            }

            return HalNone.NONE;
        }
    });

    private static final Reference __write__ = new Reference(new Builtin("write", new Params.ParamGroup("args")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray a = (HalArray) args.get("args");

            for(HalObject arg : a.value)
                System.out.print(arg);

            return HalNone.NONE;
        }
    });
    
    private static final Reference __range__ = new Reference(new Builtin("range",
            new Params.Param("end"),
            new Params.Keyword("start", new HalInteger(0)),
            new Params.Keyword("step", new HalInteger(1)))
    {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            int ini = ((HalInteger)args.get("start")).getValue();
            int end = ((HalInteger)args.get("end")).getValue();
            int step = ((HalInteger)args.get("step")).getValue();

            HalArray arr = new HalArray();
            for (int i = ini; i < end; i+=step) {
                arr.methodcall("__append!__", new HalInteger(i));
            }
            return arr;
        }
    });

    private static final Reference __method_missing__ = new Reference(new Builtin("method_missing",
            new Params.Param("name"),
            new Params.ParamGroup("args"),
            new Params.KeywordGroup("kwargs")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            throw new NameException(((HalString)args.get("name")).getValue());
        }
    });

    public static final HalClass klass = new HalClass("Kernel", HalObject.klass,
            __print__,
            __write__,
            __range__,
            __method_missing__
    );
}

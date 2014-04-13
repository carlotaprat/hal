package hal.interpreter.types.numeric;

import hal.interpreter.Reference;
import hal.interpreter.core.BuiltinMethod;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.TypeException;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.HalString;

public class HalInteger extends HalNumber
{
    private static final String classId = "Integer";

    public HalInteger(Integer i)
    {
        super(i);
    }
    
    public HalInteger(float f) {
        super((int) f);
    }
    
    public HalInteger(double d) {
        super((int) d);
    }
    
    public HalBoolean bool() {
        return new HalBoolean(toInteger() == 0);
    }
    
    public HalNumber add(HalNumber n) {
        // TODO: Pensar en alguna cosa per tal que la conversió la faci java
        // alguna cosa com getValue per evitar de fer l'if
        if (n instanceof HalFloat)
            return new HalFloat(toInteger() + n.toFloat());

        return new HalInteger(toInteger() + n.toInteger());
        
    }
    private static final Reference __add__ = new Reference(new BuiltinMethod("__add__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();
            
            if (args[0] instanceof HalFloat)
                return new HalFloat(((HalInteger)instance).toFloat() + ((HalFloat)args[0]).toFloat());

            return new HalInteger(((HalInteger)instance).toInteger() + ((HalInteger)args[0]).toInteger());
        }
    });

    private static final Reference __sub__ = new Reference(new BuiltinMethod("__sub__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();
            
            if (args[0] instanceof HalFloat)
                return new HalFloat(((HalInteger)instance).toFloat() - ((HalFloat)args[0]).toFloat());

            return new HalInteger(((HalInteger)instance).toInteger() - ((HalInteger)args[0]).toInteger());
        }
    });   
    
    
    private static final Reference __lt__ = new Reference(new BuiltinMethod("__lt__") {
        @Override
        public HalObject call(HalObject instance, HalObject... args) {
            if(args.length != 1)
                throw new TypeException();

            return new HalBoolean(((HalInteger)instance.value).toInteger()
                    < ((HalNumber)args[0]).toFloat());
        }
    });
    
    private static final ReferenceRecord record = new ReferenceRecord(classId, HalNumber.record,
            __add__,
            __sub__
            
            );
    
    public ReferenceRecord getRecord() {
        return record;
    }

    @Override
    public HalNumber neg() {
        return new HalInteger(-toInteger());
    }

}

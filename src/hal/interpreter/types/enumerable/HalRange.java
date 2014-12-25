package hal.interpreter.types.enumerable;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.InternalLambda;
import hal.interpreter.core.Params;
import hal.interpreter.types.HalClass;
import hal.interpreter.types.HalMethod;
import hal.interpreter.types.HalNone;
import hal.interpreter.types.HalObject;
import hal.interpreter.types.numeric.HalInteger;
import hal.interpreter.types.HalBoolean;
import hal.interpreter.core.data.Range;
import hal.interpreter.exceptions.KeyException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class HalRange extends HalEnumerable<Range>
{
    public HalRange() {
        value = new Range();
    }

    public HalRange(HalObject start, HalObject end, HalObject include) {
        this();
        init(start, end, include);
    }

    public HalRange(HalObject start, HalObject end, HalObject step, HalObject include) {
        this(start, end, include);
        value.step = step.toInteger();
    }

    public void init(HalObject start, HalObject end, HalObject include) {
        value.start = start.toInteger();
        value.end = end.toInteger();
        value.include = include.toBoolean();
    }

    public HalString str() {
        return new HalString(value.start + (value.include ? ".." : "...") + value.end);
    }

    public HalObject getitem(HalObject index) {
        int item = value.start + value.step * ((HalInteger) index).value;

        if(item == value.end && !value.include || item > value.end) {
            throw new KeyException(index.toString());
        }

        return new HalInteger(item);
    }

    public HalInteger size() {
        int size = (value.end - value.start + (value.include ? 1 : 0)) / value.step;

        return new HalInteger(size < 0 ? 0 : size);
    }
    
    private static final Reference __each__ = new Reference(new Builtin("each") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalObject last = HalNone.NONE;
            HalRange range = (HalRange) instance;

            int end = range.value.end + (range.value.include ? 1 : 0);

            if(lambda.getArity() < 2) {
                for (int i = range.value.start; i < end; i += range.value.step) {
                    last = lambda.call(range, null, new HalInteger(i));

                    if(lambda.isBreakRequested())
                        return last;
                }
            } else {
                int index = 0;
                for(int i = range.value.start; i < end; i += range.value.step) {
                    last = lambda.call(range, null, new HalInteger(index), new HalInteger(i));

                    if(lambda.isBreakRequested())
                        return last;

                    index++;
                }
            }

            return last;
        }
    });

    private static final Reference __init__ = new Reference(new Builtin("init",
        new Params.Param("start"),
        new Params.Param("end"),
        new Params.Keyword("include", new HalBoolean(false))) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            ((HalRange) instance).init(args.get("start"), args.get("end"), args.get("include"));
            return HalNone.NONE;
        }
    });

    public static final HalClass klass = new HalClass("Range", HalEnumerable.klass,
            __each__
    ) {
        public HalObject newInstance(final HalClass instklass) {
            return new HalRange() {
                public HalClass getKlass() { return instklass; }
            };
        }
    };

    public HalClass getKlass() { return HalRange.klass; }
}

package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.exceptions.OSException;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalString;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;


public class HalStringStream extends HalObject<String> {
    public HalStringStream() {
        super("");
    }

    public HalBoolean bool() {
        return new HalBoolean(true);
    }
    public HalString str() {
        return new HalString(value);
    }

    private static final Reference __print__ = new Reference(new Builtin("print", new Params.ParamGroup("stuff")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray stuff = (HalArray) args.get("stuff");
            HalObject s = stuff.methodcall("__join__", new HalString("\n"));

            ((HalStringStream)instance).value += s.toString() + "\n";
            return HalNone.NONE;
        }
    });

    private static final Reference __write__ = new Reference(new Builtin("write", new Params.ParamGroup("stuff")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray stuff = (HalArray) args.get("stuff");
            HalObject s = stuff.methodcall("__join__");

            ((HalStringStream)instance).value += s.toString();
            return HalNone.NONE;
        }
    });

    public static final HalClass klass = new HalClass("StringStream", HalObject.klass,
            __print__,
            __write__
    ) {
        public HalObject newInstance(final HalClass instklass) {
            return new HalStringStream() {
                public HalClass getKlass() {
                    return instklass;
                }
            };
        }
    };

    public HalClass getKlass() { return HalStringStream.klass;  }
}

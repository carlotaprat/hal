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


public class HalFile extends HalObject<PrintWriter> {
    public HalFile(PrintWriter writer) {
        super(writer);
    }

    public HalBoolean bool() {
        return new HalBoolean(true);
    }

    private static final Reference __open__ = new Reference(new Builtin("open", new Params.Param("path")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            try {
                HalFile file = new HalFile(new PrintWriter(args.get("path").toString(), "UTF-8"));

                if(lambda != null) {
                    lambda.call(instance, null, file);
                    file.value.close();
                }

                return file;
            } catch (FileNotFoundException e) {
                throw new OSException(e.getMessage());
            } catch (UnsupportedEncodingException e) {
                throw new OSException(e.getMessage());
            }
        }
    });

    private static final Reference __print__ = new Reference(new Builtin("print", new Params.ParamGroup("stuff")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray stuff = (HalArray) args.get("stuff");
            HalObject s = stuff.methodcall("__join__", new HalString("\n"));

            ((HalFile)instance).value.println(s.toString());
            return HalNone.NONE;
        }
    });

    private static final Reference __write__ = new Reference(new Builtin("write", new Params.ParamGroup("stuff")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            HalArray stuff = (HalArray) args.get("stuff");
            HalObject s = stuff.methodcall("__join__");

            ((HalFile)instance).value.print(s.toString());
            return HalNone.NONE;
        }
    });

    private static final Reference __close__ = new Reference(new Builtin("close!") {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            ((HalFile)instance).value.close();
            return HalNone.NONE;
        }
    });

    public static final HalClass klass = new HalClass("File", HalObject.klass, new Reference[]{
            __print__,
            __write__,
            __close__
    },
            __open__);

    public HalClass getKlass() { return HalFile.klass;  }
}

package hal.interpreter.types;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.OSException;
import hal.interpreter.types.enumerable.HalString;
import hal.interpreter.types.numeric.HalInteger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;


public class HalProcess extends HalObject<Process>
{
    public HalProcess(Process p) {
        super(p);
        ReferenceRecord record = getRecord();

        record.defineVariable("output", new HalString(streamToString(p.getInputStream())));
        record.defineVariable("error", new HalString(streamToString(p.getErrorStream())));
        record.defineVariable("status", new HalInteger(p.exitValue()));
    }

    private String streamToString(InputStream stream) {
        BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;

        try {
            boolean first = true;
            while ( (line = br.readLine()) != null) {
                if(!first)
                    builder.append(System.getProperty("line.separator"));
                else
                    first = false;

                builder.append(line);
            }
        } catch (IOException e) {
            return "";
        }

        return builder.toString();
    }

    public HalBoolean bool() {
        return new HalBoolean(value.exitValue() == 0);
    }


    private static Reference __exec__ = new Reference(new Builtin("exec", new Params.Param("command")) {
        @Override
        public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
            String cmd = ((HalString) args.get("command").methodcall("__str__")).value;

            try {
                String[] cmds = {"/bin/sh", "-c", cmd};
                Process p = Runtime.getRuntime().exec(cmds);
                p.waitFor();
                return new HalProcess(p);
            } catch (IOException e) {
                throw new OSException(e.getMessage());
            } catch (InterruptedException e) {
                throw new OSException(e.getMessage());
            }
        }
    });

    public static final HalClass klass = new HalClass("Process", HalObject.klass, new Reference[]{},
            __exec__);

    public HalClass getKlass() {
        return HalProcess.klass;
    }
}

package hal.interpreter.modules;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.*;
import hal.interpreter.types.numeric.HalInteger;
import org.lwjgl.input.Keyboard;


public class input extends HalModule {

    private static final HalClass HalKeyboard = new HalClass("Keyboard", HalObject.klass) {
        public void defineKlass(ReferenceRecord inst, ReferenceRecord stat) {
            // Static methods
            // Keyboard.down?
            stat.defineBuiltin(new Reference(new Builtin("down?",
                    new Params.Param("key")) {
                @Override
                public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                    return new HalBoolean(Keyboard.isKeyDown(((HalInteger)args.get("key")).value));
                }
            }));

            // Define keys
            stat.defineVariable("KEY_LEFT", new HalInteger(Keyboard.KEY_LEFT));
            stat.defineVariable("KEY_RIGHT", new HalInteger(Keyboard.KEY_RIGHT));
            stat.defineVariable("KEY_UP", new HalInteger(Keyboard.KEY_UP));
            stat.defineVariable("KEY_DOWN", new HalInteger(Keyboard.KEY_DOWN));
        }
    };

    public input(HalPackage pkg){
        super("input", pkg);

        ReferenceRecord module = getInstanceRecord();

        module.defineVariable("Keyboard", HalKeyboard);
    }
}

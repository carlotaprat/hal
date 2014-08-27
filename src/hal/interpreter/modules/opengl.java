package hal.interpreter.modules;

import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.OSException;
import hal.interpreter.types.*;
import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


public class opengl extends HalModule {
    public opengl(HalPackage pkg){
        super("opengl", pkg);

        ReferenceRecord module = getInstanceRecord();

        module.defineMethod(new Builtin("test_display") {
            @Override
            public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                try {
                    Display.setDisplayMode(new DisplayMode(1280, 720));
                    Display.setLocation(0, 0);
                    Display.create();
                } catch (LWJGLException e) {
                    throw new OSException(e.getMessage());
                }

                if(lambda == null) {
                    while(!Display.isCloseRequested()) {
                        Display.update();
                    }
                } else {
                    while(!Display.isCloseRequested()) {
                        lambda.call(instance, null);
                        Display.update();
                    }
                }

                Display.destroy();
                return HalNone.NONE;
            }
        });
    }
}

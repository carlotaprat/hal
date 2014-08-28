package hal.interpreter.modules;

import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.exceptions.OSException;
import hal.interpreter.types.*;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.enumerable.HalString;
import hal.interpreter.types.numeric.HalInteger;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;


public class opengl extends HalModule {
    private int fps = 1;
    private long lastFPS;
    private long lastFrame;

    private int getDelta() {
        long time = getTime();
        int delta = (int)(time - lastFrame);
        lastFrame = time;

        return delta;
    }

    private long getTime() {
        return (Sys.getTime() * 1000) / Sys.getTimerResolution();
    }

    private void updateFPS() {
        long time = getTime();

        if(time - lastFPS > 1000) {
            String current = Display.getTitle().split("\\s\\|\\s")[0];
            Display.setTitle(current + " | FPS: " + (fps-1));
            fps = 0;
            lastFPS = time;
        }

        fps++;
    }

    public opengl(HalPackage pkg){
        super("opengl", pkg);

        ReferenceRecord module = getInstanceRecord();

        module.defineMethod(new Builtin("display",
                new Params.Param("title"),
                new Params.Param("width"),
                new Params.Param("height"),
                new Params.Keyword("location", HalNone.NONE)) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                HalString title = (HalString) args.get("title");
                HalInteger width = (HalInteger) args.get("width");
                HalInteger height = (HalInteger) args.get("height");
                HalObject location = args.get("location");

                try {
                    Display.setTitle(title.value);
                    Display.setDisplayMode(new DisplayMode(width.value, height.value));

                    if(location == HalNone.NONE) {
                        Display.setLocation(0, 0);
                    }
                    else {
                        HalArray locationCoords = (HalArray) location;
                        Display.setLocation(
                                ((HalInteger)locationCoords.value.get(0)).value,
                                ((HalInteger)locationCoords.value.get(1)).value
                        );
                    }

                    Display.create();
                } catch (LWJGLException e) {
                    throw new OSException(e.getMessage());
                }

                if(lambda != null) {
                    lambda.call(instance, null);
                    Display.destroy();
                }

                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("close") {
            @Override
            public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                Display.destroy();
                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("draw",
                new Params.Keyword("fps", new HalInteger(60))) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                int fps = ((HalInteger) args.get("fps")).value;

                getDelta();

                while(!Display.isCloseRequested()) {
                    // We need to compute delta here
                    int delta = getDelta();

                    // New calculations and drawing are performed by HAL using delta
                    lambda.call(instance, null, new HalInteger(delta));

                    // Update and sync
                    updateFPS();
                    Display.update();
                    Display.sync(fps);
                }

                return HalNone.NONE;
            }
        });
    }
}

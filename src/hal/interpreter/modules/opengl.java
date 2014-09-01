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
import hal.interpreter.types.numeric.HalNumber;
import org.lwjgl.LWJGLException;
import org.lwjgl.Sys;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;
import org.lwjgl.opengl.GL11;


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
            Display.setTitle(current + " | " + (fps-1) + " fps");
            fps = 0;
            lastFPS = time;
        }

        fps++;
    }

    public opengl(HalPackage pkg){
        super("opengl", pkg);

        ReferenceRecord module = getInstanceRecord();

        // GL variables
        module.defineVariable("DEPTH_TEST", new HalInteger(GL11.GL_DEPTH_TEST));

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
                    Display.setVSyncEnabled(true);

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
        
        module.defineMethod(new Builtin("projection") {
            @Override
            public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                GL11.glMatrixMode(GL11.GL_PROJECTION);
                lambda.call(instance, null);
                GL11.glMatrixMode(GL11.GL_MODELVIEW);

                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("ortho",
                new Params.Param("left"),
                new Params.Param("right"),
                new Params.Param("bottom"),
                new Params.Param("top"),
                new Params.Param("z_near"),
                new Params.Param("z_far")) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                GL11.glOrtho(
                        ((HalNumber)args.get("left")).toFloat(),
                        ((HalNumber)args.get("right")).toFloat(),
                        ((HalNumber)args.get("bottom")).toFloat(),
                        ((HalNumber)args.get("top")).toFloat(),
                        ((HalNumber)args.get("z_near")).toFloat(),
                        ((HalNumber)args.get("z_far")).toFloat()
                );

                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("disable",
                new Params.Param("cap")) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                GL11.glDisable(((HalInteger)args.get("cap")).value);

                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("clear") {
            @Override
            public HalObject call(HalObject instance, HalMethod lambda, Arguments args) {
                GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("color",
                new Params.Param("r"),
                new Params.Param("g"),
                new Params.Param("b")) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                GL11.glColor3d(
                        ((HalNumber)args.get("r")).toFloat(),
                        ((HalNumber)args.get("g")).toFloat(),
                        ((HalNumber)args.get("b")).toFloat()
                );

                return HalNone.NONE;
            }
        });

        // Draws 2D rectangle
        module.defineMethod(new Builtin("rectangle",
                new Params.Param("x"),
                new Params.Param("y"),
                new Params.Param("width"),
                new Params.Param("height")) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                double x = ((HalNumber)args.get("x")).toFloat();
                double y = ((HalNumber)args.get("y")).toFloat();
                double width = ((HalNumber)args.get("width")).toFloat();
                double height = ((HalNumber)args.get("height")).toFloat();

                GL11.glBegin(GL11.GL_QUADS);
                GL11.glVertex2d(x, y);
                GL11.glVertex2d(x, y + height);
                GL11.glVertex2d(x + width, y + height);
                GL11.glVertex2d(x + width, y);
                GL11.glEnd();

                return HalNone.NONE;
            }
        });

        module.defineMethod(new Builtin("line",
                new Params.Param("x1"),
                new Params.Param("y1"),
                new Params.Param("x2"),
                new Params.Param("y2")) {
            @Override
            public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                double x1 = ((HalNumber)args.get("x1")).toFloat();
                double y1 = ((HalNumber)args.get("y1")).toFloat();
                double x2 = ((HalNumber)args.get("x2")).toFloat();
                double y2 = ((HalNumber)args.get("y2")).toFloat();

                GL11.glBegin(GL11.GL_LINES);
                GL11.glVertex2d(x1, y1);
                GL11.glVertex2d(x2, y2);
                GL11.glEnd();

                return HalNone.NONE;
            }
        });
    }
}

package hal.interpreter.modules;

import hal.interpreter.Reference;
import hal.interpreter.core.Arguments;
import hal.interpreter.core.Builtin;
import hal.interpreter.core.Params;
import hal.interpreter.core.ReferenceRecord;
import hal.interpreter.types.*;
import hal.interpreter.types.enumerable.HalArray;
import hal.interpreter.types.numeric.HalInteger;
import hal.interpreter.types.numeric.HalNumber;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;


public class input extends HalModule {
    enum KeyStatus {
        UP, DOWN, DOWN_STILL, DOWN_RELEASED, DOWN_UPDATED
    }

    private static final HalClass HalKeyboard = new HalClass("Keyboard", HalObject.klass) {
        class Key {
            public int elapsedTime;
            public KeyStatus status;

            public Key() {
                elapsedTime = -1;
                status = KeyStatus.UP;
            }
        }

        private HashMap<Integer, Key> keyMap;
        private int updateTime;
        private int updateLimit;

        public void defineKlass(ReferenceRecord inst, ReferenceRecord stat) {
            keyMap = new HashMap<Integer, Key>();
            updateTime = 0;
            updateLimit = 20;

            // Static methods
            // Keyboard.down?
            stat.defineBuiltin(new Reference(new Builtin("down?",
                    new Params.Param("key")) {
                @Override
                public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                    return new HalBoolean(Keyboard.isKeyDown(((HalInteger)args.get("key")).value));
                }
            }));

            stat.defineBuiltin(new Reference(new Builtin("poll",
                    new Params.Param("keys"),
                    new Params.Param("delta")) {
                @Override
                public HalObject mcall(HalObject instance, HalMethod lambda, Arguments args) {
                    HalArray keys = (HalArray) args.get("keys");
                    int delta = ((HalNumber) args.get("delta")).toInteger();

                    for(HalObject key : keys.value) {
                        int keyval = ((HalInteger)key).value;

                        if(! keyMap.containsKey(keyval))
                            keyMap.put(keyval, new Key());

                        Key keyState = keyMap.get(keyval);

                        if(Keyboard.isKeyDown(keyval)) {
                            if(keyState.status == KeyStatus.UP) {
                                keyState.status = KeyStatus.DOWN;
                            } else if(keyState.status == KeyStatus.DOWN_UPDATED) {
                                if(keyState.elapsedTime == -1)
                                    keyState.elapsedTime = 0;
                                else
                                    keyState.elapsedTime += delta;

                                if(keyState.elapsedTime > updateLimit * 5)
                                    keyState.status = KeyStatus.DOWN_STILL;
                            }
                        } else {
                            keyState.elapsedTime = -1;

                            if(keyState.status == KeyStatus.DOWN)
                                keyState.status = KeyStatus.DOWN_RELEASED;
                            else if(keyState.status != KeyStatus.DOWN_RELEASED)
                                keyState.status = KeyStatus.UP;
                        }
                    }

                    updateTime += delta;

                    if(updateTime >= updateLimit) {
                        for(HalObject key : keys.value) {
                            Key keyState = keyMap.get(((HalInteger)key).value);

                            if(keyState.status != KeyStatus.UP && keyState.status != KeyStatus.DOWN_UPDATED) {
                                lambda.call(this, null, key, new HalBoolean(keyState.status == KeyStatus.DOWN_STILL));

                                if(keyState.status == KeyStatus.DOWN)
                                    keyState.status = KeyStatus.DOWN_UPDATED;
                                else if(keyState.status == KeyStatus.DOWN_RELEASED)
                                    keyState.status = KeyStatus.UP;
                            }
                        }

                        updateTime = 0;
                    }

                    return HalNone.NONE;
                }
            }));

            // Define keys
            stat.defineVariable("KEY_LEFT", new HalInteger(Keyboard.KEY_LEFT));
            stat.defineVariable("KEY_RIGHT", new HalInteger(Keyboard.KEY_RIGHT));
            stat.defineVariable("KEY_UP", new HalInteger(Keyboard.KEY_UP));
            stat.defineVariable("KEY_DOWN", new HalInteger(Keyboard.KEY_DOWN));
            stat.defineVariable("KEY_SPACE", new HalInteger(Keyboard.KEY_SPACE));
        }
    };

    public input(HalPackage pkg){
        super("input", pkg);

        ReferenceRecord module = getInstanceRecord();

        module.defineVariable("Keyboard", HalKeyboard);
    }
}

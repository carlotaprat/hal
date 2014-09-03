package hal.interpreter.exceptions;


import hal.interpreter.types.HalObject;

public class AttributeException extends RuntimeException
{
    public AttributeException(HalObject obj, String attr) {
        super(obj + " has no attribute '" + attr + "'");
    }
}

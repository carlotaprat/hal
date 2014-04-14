package hal.interpreter.exceptions;


public class AbstractClassException extends RuntimeException
{
    public AbstractClassException(String name) {
        super("Instance creation of abstract class: " + name);
    }
}

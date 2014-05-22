package hal.interpreter.exceptions;


public class NewNotSupportedException extends RuntimeException
{
    public NewNotSupportedException() {
        super("Method new not supported");
    }
}

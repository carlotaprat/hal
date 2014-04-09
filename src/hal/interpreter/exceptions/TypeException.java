package hal.interpreter.exceptions;


public class TypeException extends RuntimeException
{
    public TypeException() {
        this("Unknown.");
    }

    public TypeException(String message) {
        super(message);
    }
}

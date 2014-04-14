package hal.interpreter.exceptions;


public class NameException extends RuntimeException {
    public NameException(String name) {
        super("Name " + name + " not defined");
    }
}

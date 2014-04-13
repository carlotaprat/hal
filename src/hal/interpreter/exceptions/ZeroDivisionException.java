package hal.interpreter.exceptions;


public class ZeroDivisionException extends RuntimeException {
    public ZeroDivisionException() {
        this("Can't divide by zero.");
    }

    public ZeroDivisionException(String message) {
        super(message);
    }
}

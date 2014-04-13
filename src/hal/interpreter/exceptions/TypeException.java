package hal.interpreter.exceptions;


public class TypeException extends RuntimeException
{
    public TypeException() {
        this("Unknown.");
    }

    public TypeException(String message) {
        super(message);
    }

    public static TypeException fromCastException(ClassCastException e) {
        String[] parts = e.getMessage().split(" ");

        return new TypeException("Implicit cast of " + getHalClass(parts[0]) + " to " +
                getHalClass(parts[parts.length-1]));
    }

    private static String getHalClass(String fullClass) {
        String[] parts = fullClass.split("\\.");

        return parts[parts.length-1].substring(3);
    }
}

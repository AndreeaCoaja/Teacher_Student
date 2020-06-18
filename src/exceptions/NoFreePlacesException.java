package exceptions;

public class NoFreePlacesException extends RuntimeException {
    public NoFreePlacesException(String message) {
        super(message);
    }
}

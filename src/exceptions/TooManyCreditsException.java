package exceptions;

public class TooManyCreditsException extends RuntimeException {
    public TooManyCreditsException(String message) {
        super(message);
    }
}

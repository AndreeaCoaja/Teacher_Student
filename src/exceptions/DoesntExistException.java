package exceptions;

public class DoesntExistException extends RuntimeException{
     public DoesntExistException(String message) {
        super(message);
    }
}

package payroll;

public class OrderNotFoundException extends RuntimeException {
    private final String message;

    public OrderNotFoundException(String message) {
        super(message);
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}

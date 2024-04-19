package mpadillamarcos.javaspringbank.domain.exception;

public class TransactionNotAllowedException extends RuntimeException {

    public TransactionNotAllowedException(String message) {
        super(message);
    }
}

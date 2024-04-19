package mpadillamarcos.javaspringbank.web.exception;

import mpadillamarcos.javaspringbank.domain.exception.AccessDeniedException;
import mpadillamarcos.javaspringbank.domain.exception.InsufficientBalanceException;
import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import mpadillamarcos.javaspringbank.domain.exception.TransactionNotAllowedException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(NotFoundException exception) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> accessDenied(AccessDeniedException exception) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> insufficientBalance(InsufficientBalanceException exception) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(Map.of("message", exception.getMessage()));
    }

    @ExceptionHandler(TransactionNotAllowedException.class)
    public ResponseEntity<Map<String, String>> transactionNotAllowed(TransactionNotAllowedException exception) {
        return ResponseEntity
                .status(FORBIDDEN)
                .body(Map.of("message", exception.getMessage()));
    }
}

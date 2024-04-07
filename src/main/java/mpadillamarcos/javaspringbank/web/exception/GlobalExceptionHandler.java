package mpadillamarcos.javaspringbank.web.exception;

import mpadillamarcos.javaspringbank.domain.exception.NotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, String>> notFound(NotFoundException exception) {
        return ResponseEntity
                .status(NOT_FOUND)
                .body(Map.of("message", exception.getMessage()));
    }
}

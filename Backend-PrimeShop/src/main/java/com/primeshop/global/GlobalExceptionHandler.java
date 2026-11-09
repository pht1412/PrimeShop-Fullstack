package com.primeshop.global;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(Map.of(
                    "status", "ERROR",
                    "message", ex.getReason() == null ? "Yêu cầu không hợp lệ." : ex.getReason()
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAllExceptions(Exception ex) {
        return ResponseEntity
                .status(500)
                .body(Map.of(
                    "status", "ERROR",
                    "message", "Lỗi hệ thống nội bộ: " + ex.getMessage()
                ));
    }
}

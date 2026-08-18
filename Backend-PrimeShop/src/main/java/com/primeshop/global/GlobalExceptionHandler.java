package com.primeshop.global;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {
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

package ir.milad.DocVisitApp.infra.web;

import ir.milad.DocVisitApp.domain.ApplicationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Map;

@ControllerAdvice
public class ControllerExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(value = ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(
            ApplicationException ex, WebRequest request
    ) {
        return handleExceptionInternal(ex, Map.of("error", ex.getMessage()), new HttpHeaders(), HttpStatus.BAD_REQUEST, request);
    }
}

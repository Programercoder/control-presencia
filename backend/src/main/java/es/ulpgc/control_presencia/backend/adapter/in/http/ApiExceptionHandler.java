package es.ulpgc.control_presencia.backend.adapter.in.http;

import es.ulpgc.control_presencia.backend.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;


@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler({PersonNotFoundException.class, ScheduleNotFoundException.class})
    ResponseEntity<ApiError> notFound(DomainException e) {
        return build(HttpStatus.NOT_FOUND, e);
    }

    @ExceptionHandler({DuplicateClockEventException.class, DuplicateScheduleException.class,
            AssignmentOverlapException.class})
    ResponseEntity<ApiError> conflict(DomainException e) {
        return build(HttpStatus.CONFLICT, e);
    }

    @ExceptionHandler(DomainException.class)
    ResponseEntity<ApiError> unprocessable(DomainException e) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, e);
    }

    @ExceptionHandler({HttpMessageNotReadableException.class, MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class})
    ResponseEntity<ApiError> badRequest(Exception e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiError("BAD_REQUEST", "Malformed request: check the JSON body, dates (yyyy-MM-dd) and required parameters"));
    }

    private ResponseEntity<ApiError> build(HttpStatus status, DomainException e) {
        return ResponseEntity.status(status).body(new ApiError(code(e), e.getMessage()));
    }

    /** PersonNotFoundException -> PERSON_NOT_FOUND; plain DomainException -> INVALID_DATA. */
    private String code(DomainException e) {
        String name = e.getClass().getSimpleName().replace("Exception", "");
        if (name.equals("Domain")) return "INVALID_DATA";
        return name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
    }
}

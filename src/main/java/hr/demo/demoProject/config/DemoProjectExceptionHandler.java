package hr.demo.demoProject.config;

import hr.demo.demoProject.api.model.ErrorResponse;
import hr.demo.demoProject.config.exception.DemoProjectException;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Log4j2
public class DemoProjectExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({Exception.class})
    public ResponseEntity<ErrorResponse> generalException(Exception e) {
        log.error("", e);
        ErrorResponse response = ErrorResponse.builder()
                .title(e.getMessage())
                .status(500)
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler({DemoProjectException.class})
    public ResponseEntity<ErrorResponse> demoProjectException(DemoProjectException e) {
        log.error("", e);
        ErrorResponse response = ErrorResponse.builder()
                .title(e.getMessage())
                .status(400)
                .build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        log.warn("", ex);

        ErrorResponse response = ErrorResponse.builder()
                .title("Validation failed")
                .status(status.value())
                .instance(request.getDescription(true))
                .build();
        StringBuilder sb = new StringBuilder();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            sb.append(fieldError.getField());
            sb.append(": ");
            sb.append(fieldError.getDefaultMessage());
            sb.append("; ");
            response.detail(sb.toString());
        }

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }
}

package ru.fox.orion.core.exception.handler;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.servlet.NoHandlerFoundException;
import ru.fox.orion.config.MessageTranslator;
import ru.fox.orion.model.dto.ApiError;
import ru.fox.orion.model.dto.ErrorResponse;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@ControllerAdvice
public class ValidationExceptionHandler extends BaseExceptionHandler {

    private static final String COLON_SPACE = ": ";
    private static final String COMMA_SPACE = ", ";
    private static final String SPACE_APOSTROPHES = " '";
    private static final String APOSTROPHE = "'";
    private static final String SERVICE_UNAVAILABLE = "service.unavailable";

    private DataIntegrityViolationException ex;

    public ValidationExceptionHandler(MessageTranslator messageTranslator) {
        super(messageTranslator);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(ConstraintViolationException ex) {
        logError(ex, HttpStatus.BAD_REQUEST);
        List<String> messages = new ArrayList<>();
        for (ConstraintViolation<?> constraintViolation : ex.getConstraintViolations()) {
            String param = "";
            for (var node : constraintViolation.getPropertyPath()) {
                param = node.getName();
            }
            messages.add(param + COLON_SPACE + tryTranslate(constraintViolation.getMessage(), null));
        }
        return new ResponseEntity<>(
                new ErrorResponse(
                        new ApiError(HttpStatus.BAD_REQUEST.value(), String.join(COMMA_SPACE, messages))
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    protected ResponseEntity<Object> handleConstraintViolation(DataIntegrityViolationException ex) {
        this.ex = ex;
        String errorMessage = ex.getRootCause().getMessage();
        String conflictingKey = extractConflictingKey(errorMessage);
        logError(ex, HttpStatus.BAD_REQUEST);
        String userFriendlyErrorMessage = "Ошибка: Значение ключа уже существует в базе данных. Ключ: " + conflictingKey;
        return new ResponseEntity<>(
                new ErrorResponse(
                        new ApiError(HttpStatus.BAD_REQUEST.value(), userFriendlyErrorMessage)
                ),
                HttpStatus.BAD_REQUEST
        );
    }

    // Метод для извлечения ключа, вызвавшего конфликт, из сообщения об ошибке
    private String extractConflictingKey(String errorMessage) {
        int startIndex = errorMessage.indexOf("(") + 1;
        int endIndex = errorMessage.indexOf(")");
        return errorMessage.substring(startIndex, endIndex);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        List<String> messages = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = tryTranslate(error.getDefaultMessage(), null);
            messages.add(fieldName + COLON_SPACE + errorMessage);
        }
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), String.join(COMMA_SPACE, messages))
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @NonNull
    protected ResponseEntity<Object> handleBindException(@NonNull BindException ex,
                                                         @NonNull HttpHeaders headers,
                                                         @NonNull HttpStatusCode status,
                                                         @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        List<String> messages = new ArrayList<>();
        for (ObjectError error : ex.getBindingResult().getAllErrors()) {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            messages.add(fieldName + COLON_SPACE + errorMessage);
        }

        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), String.join(COMMA_SPACE, messages))
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @ExceptionHandler
    protected ResponseEntity<Object> handleException(Exception ex) {
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), tryTranslate(SERVICE_UNAVAILABLE, null))
        );
        logError(ex, HttpStatus.valueOf(response.getError().getCode()));
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(@NonNull HttpRequestMethodNotSupportedException ex,
                                                                         @NonNull HttpHeaders headers,
                                                                         @NonNull HttpStatusCode status,
                                                                         @NonNull WebRequest request) {
        logError(ex, HttpStatus.METHOD_NOT_ALLOWED);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.METHOD_NOT_ALLOWED.value(),
                        messageTranslator.get("method.not.allowed", new Object[]{ex.getMethod()})
                )
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(@NonNull HttpMediaTypeNotSupportedException ex,
                                                                     @NonNull HttpHeaders headers,
                                                                     @NonNull HttpStatusCode status,
                                                                     @NonNull WebRequest request) {
        logError(ex, HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.UNSUPPORTED_MEDIA_TYPE.value(),
                        messageTranslator.get("media.type.not.supported", new Object[]{ex.getContentType()})
                )
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(@NonNull HttpMediaTypeNotAcceptableException ex,
                                                                      @NonNull HttpHeaders headers,
                                                                      @NonNull HttpStatusCode status,
                                                                      @NonNull WebRequest request) {
        logError(ex, HttpStatus.NOT_ACCEPTABLE);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.NOT_ACCEPTABLE.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleMissingPathVariable(@NonNull MissingPathVariableException ex,
                                                               @NonNull HttpHeaders headers,
                                                               @NonNull HttpStatusCode status,
                                                               @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        var parameter = ex.getParameter();
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(),
                        messageTranslator.get("path.var.not.present",
                                new Object[]{parameter.getParameterType() + SPACE_APOSTROPHES + parameter.getParameterName() + APOSTROPHE}))
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleMissingServletRequestParameter(@NonNull MissingServletRequestParameterException ex,
                                                                          @NonNull HttpHeaders headers,
                                                                          @NonNull HttpStatusCode status,
                                                                          @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(),
                        messageTranslator.get("param.not.present",
                                new Object[]{ex.getParameterType() + SPACE_APOSTROPHES + ex.getParameterName() + APOSTROPHE}))
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleServletRequestBindingException(@NonNull ServletRequestBindingException ex,
                                                                          @NonNull HttpHeaders headers,
                                                                          @NonNull HttpStatusCode status,
                                                                          @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleConversionNotSupported(@NonNull ConversionNotSupportedException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleTypeMismatch(@NonNull TypeMismatchException ex,
                                                        @NonNull HttpHeaders headers,
                                                        @NonNull HttpStatusCode status,
                                                        @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(@NonNull HttpMessageNotReadableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        String message = messageTranslator.get("message.not.readable") + ". " + ex.getLocalizedMessage();
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), message)
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotWritable(@NonNull HttpMessageNotWritableException ex,
                                                                  @NonNull HttpHeaders headers,
                                                                  @NonNull HttpStatusCode status,
                                                                  @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(@NonNull AsyncRequestTimeoutException ex,
                                                                        @NonNull HttpHeaders headers,
                                                                        @NonNull HttpStatusCode status,
                                                                        @NonNull WebRequest webRequest) {
        logError(ex, HttpStatus.REQUEST_TIMEOUT);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.REQUEST_TIMEOUT.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(@NonNull NoHandlerFoundException ex,
                                                                   @NonNull HttpHeaders headers,
                                                                   @NonNull HttpStatusCode status,
                                                                   @NonNull WebRequest request) {
        logError(ex, HttpStatus.NOT_FOUND);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.NOT_FOUND.value(), ex.getLocalizedMessage())
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(@NonNull Exception ex,
                                                             Object body,
                                                             @NonNull HttpHeaders headers,
                                                             @NonNull HttpStatusCode status,
                                                             @NonNull WebRequest request) {
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(), tryTranslate(SERVICE_UNAVAILABLE, null))
        );
        logError(ex, HttpStatus.valueOf(response.getError().getCode()));
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }
}

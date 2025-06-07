package ru.fox.orion.core.exception.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.NonNull;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import ru.fox.orion.config.MessageTranslator;
import ru.fox.orion.model.dto.ApiError;
import ru.fox.orion.model.dto.ErrorResponse;

import java.io.IOException;
import java.nio.file.AccessDeniedException;

import static ru.fox.orion.core.exception.ErrorMessage.HEADER_NOT_PRESENT;
import static ru.fox.orion.core.exception.ErrorMessage.PARAM_NOT_PRESENT;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
@ControllerAdvice
public class GeneralExceptionHandler extends BaseExceptionHandler {

    private static final String APOSTROPHE = "'";

    public GeneralExceptionHandler(MessageTranslator messageTranslator) {
        super(messageTranslator);
    }

    @ExceptionHandler(IOException.class)
    protected ResponseEntity<Object> handleIOException(IOException ex) {
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        tryTranslate("service.unavailable", null))
        );
        logError(ex, HttpStatus.valueOf(response.getError().getCode()));
        return new ResponseEntity<>(response, HttpStatus.valueOf(response.getError().getCode()));
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<Object> handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        tryTranslate("bad.param", new Object[]{ex.getParameter().getParameterName()})
                )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        ErrorResponse errorResponse = new ErrorResponse(
                new ApiError(HttpStatus.FORBIDDEN.value(), tryTranslate("access.denied", null))
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    protected ResponseEntity<Object> handleMissingRequestHeaderException(MissingRequestHeaderException ex) {
        MethodParameter parameter = ex.getParameter();
        ErrorResponse errorResponse = new ErrorResponse(
                new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        tryTranslate(HEADER_NOT_PRESENT.getErrorMessageKey(), new Object[]{parameter.getParameterType().getSimpleName()
                                + " '"
                                + ex.getHeaderName() + APOSTROPHE})
                )
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @NonNull
    @Override
    protected ResponseEntity<Object> handleMissingServletRequestPart(@NonNull MissingServletRequestPartException ex,
                                                                     @NonNull HttpHeaders headers,
                                                                     @NonNull HttpStatusCode status,
                                                                     @NonNull WebRequest request) {
        logError(ex, HttpStatus.BAD_REQUEST);
        ErrorResponse response = new ErrorResponse(
                new ApiError(HttpStatus.BAD_REQUEST.value(),
                        messageTranslator.get(PARAM_NOT_PRESENT.getErrorMessageKey(),
                                new Object[]{"RequestPart '" + ex.getRequestPartName() + APOSTROPHE})
                )
        );
        return new ResponseEntity<>(response, headers, HttpStatus.valueOf(response.getError().getCode()));
    }
}

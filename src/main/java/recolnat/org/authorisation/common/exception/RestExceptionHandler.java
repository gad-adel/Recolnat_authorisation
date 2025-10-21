package recolnat.org.authorisation.common.exception;


import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Locale;

import static java.util.Objects.nonNull;
import static org.springframework.http.HttpStatus.FORBIDDEN;


@RestControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
	
	@Autowired
	private MessageSource messageSource;
	


    @ExceptionHandler(AuthorisationBusinessException.class)
    public ResponseEntity<ErrorDetail> handleException(AuthorisationBusinessException authBusinessException, HttpServletRequest request){

        HttpStatus status = nonNull(authBusinessException.getHttpStatus()) ? authBusinessException.getHttpStatus() : FORBIDDEN;
    final var errorDetail =
        ErrorDetail.builder()
            .timestamp(LocalDateTime.now())
            .status(
                nonNull(authBusinessException.getHttpStatus())
                    ? authBusinessException.getHttpStatus().value()
                    : FORBIDDEN.value())
            .code(authBusinessException.getCode())
            .developerMessage(authBusinessException.getDetailMsg())
            .detail(
                messageSource.getMessage(
                    authBusinessException.getCode(), null, Locale.getDefault()))
            .message(authBusinessException.getMessage())
            .build();
        return new ResponseEntity<>(errorDetail, status);
    }

    @ExceptionHandler(AuthorisationTechnicalException.class)
    public ResponseEntity<ErrorDetail> handleException(AuthorisationTechnicalException authorisationTechnicalException, HttpServletRequest request){
    final var errorDetail =
        ErrorDetail.builder()
            .timestamp(LocalDateTime.now())
            .message(authorisationTechnicalException.getMessage())
            .status(
                nonNull(authorisationTechnicalException.getStatus())
                    ? authorisationTechnicalException.getStatus()
                    : FORBIDDEN.value())
            .code(authorisationTechnicalException.getCode())
            .developerMessage(AuthorisationTechnicalException.class.getCanonicalName())
            .build();
        return new ResponseEntity<>(errorDetail, nonNull(authorisationTechnicalException.getStatus())? HttpStatus.valueOf(authorisationTechnicalException.getStatus()): FORBIDDEN );
    }
    


    @Override
	protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, 
			HttpStatusCode status, WebRequest request) {
    	 final var errorDetail = ErrorDetail.builder()
                 .timestamp(LocalDateTime.now())
                 .code(((HttpStatus)status).name())
                 .status(status.value())
                 .detail(ex.getLocalizedMessage())
                 .developerMessage(messageSource.getMessage("error.type.mismatch", null, Locale.ENGLISH)).build();
		return new ResponseEntity<>(errorDetail, status);
	}
    
    @Override
    protected ResponseEntity<Object> handleNoHandlerFoundException(
			NoHandlerFoundException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
    	 final var errorDetail = ErrorDetail.builder()
                 .timestamp(LocalDateTime.now())
                 .status(status.value())
                 .code(((HttpStatus)status).name())
                 .detail(ex.getLocalizedMessage())
                 .developerMessage(messageSource.getMessage("error.type.notfound", null, Locale.ENGLISH)).build();
		return new ResponseEntity<>(errorDetail, status);
	}
    


    
    @SuppressWarnings("unused")
	private HttpStatus getHttpStatus(HttpServletRequest request) {
        Integer status = (Integer) request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        if (status != null) {
            return HttpStatus.valueOf(status);
        }
        String code = request.getParameter("code");
        if (code != null && !code.isBlank()) {
            return HttpStatus.valueOf(code);
        }
        return HttpStatus.INTERNAL_SERVER_ERROR;
    }


}

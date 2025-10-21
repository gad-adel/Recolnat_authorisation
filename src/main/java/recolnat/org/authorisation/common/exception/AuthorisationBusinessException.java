package recolnat.org.authorisation.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class AuthorisationBusinessException extends RuntimeException{

	private static final long serialVersionUID = -6969622773986445502L;
	private final String code;
	private final HttpStatus httpStatus;
	private final Integer status;
	private final String detailMsg;


	public AuthorisationBusinessException(final String code,final String message) {
		super(message);
		this.code = code;
		httpStatus = null;
		this.status=null;
		this.detailMsg=null;
	}
	
	public AuthorisationBusinessException(final HttpStatus httpStatus, final String code, final String message) {
		super(message);
		this.httpStatus = httpStatus;
		this.code = code;
		this.detailMsg = null;
		this.status=null;
	}

}

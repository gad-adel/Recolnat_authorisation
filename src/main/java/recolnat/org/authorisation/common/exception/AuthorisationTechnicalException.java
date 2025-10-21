package recolnat.org.authorisation.common.exception;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AuthorisationTechnicalException extends RuntimeException{

	private static final long serialVersionUID = -8349721883603219033L;
	private final String code;
    private final String detailMsg;
    private final Integer status;

    public AuthorisationTechnicalException(String code, String message){
        super(message);
        this.code = code;
        this.status=null;
        this.detailMsg=null;
    }


    public AuthorisationTechnicalException(String code, String message, int status, String detail){
        super(message);
        this.code = code;
        this.status=status;
        this.detailMsg=detail;
    }


}

package recolnat.org.authorisation.api.domain.enums;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.http.HttpStatus;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import recolnat.org.authorisation.common.exception.AuthorisationBusinessException;

@Getter
public enum PartnerType {

    PARTNER("Partner", "Partenaire"),
    DATA_PROVIDER("Data provider", "Fournisseur de données"),
    MEMBER("Member", "Membre");
    
    private String partnerEn;
    private String partnerFr;
    
    private PartnerType(String partnerEn, String partnerFr) {
        this.partnerEn = partnerEn;
        this.partnerFr = partnerFr;
    }
    
    public static PartnerType getpartnerType(@NotNull String label) {
        
    	Optional<PartnerType> partner= Arrays.asList(PartnerType.values()).parallelStream().
    	     filter(p-> p.partnerEn.equalsIgnoreCase(label) 
    	    		 	|| p.partnerFr.equalsIgnoreCase(label)
    	    		 	|| p.name().equals(label.toUpperCase())).findFirst(); 
		
		  if(partner.isPresent()) { 
			  return partner.get();
		  }else { 
			  throw new AuthorisationBusinessException(HttpStatus.NOT_FOUND,"ERR_NFE_CODE","label partner not found: " + label); 
		 }

    }
}

package recolnat.org.authorisation.api.domain;

import lombok.*;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
@Builder
public class Institution {
  private Integer id;
  private UUID institutionId;
  @NotBlank(message = "Code institution may not be empty")
  private String code;
  @NotBlank(message = "Name institution may not be empty")
  private String name;
  @NotBlank(message = "Mandatory description may not be empty")
  private String mandatoryDescription;
  private String optionalDescription;
  @NotBlank(message = "partner type may not be empty")
  private String partnerType;
  private String partnerTypeEn;
  private String partnerTypeFr;
  private String logoUrl;
  private Boolean assignable;
  private LocalDateTime createdAt;
  private String createdBy;
  private String modifiedBy;
  private LocalDateTime modifiedAt;

}

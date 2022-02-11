package ca.bc.gov.educ.api.edx.struct.v1;

import ca.bc.gov.educ.api.edx.model.v1.EdxActivationRoleEntity;
import ca.bc.gov.educ.api.edx.struct.BaseRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class EdxActivationCode extends BaseRequest implements Serializable {
  private static final long serialVersionUID = 583620260139143932L;

  String edxActivationCodeId;

  String mincode;

  String districtCode;

  String activationCode;

  String isPrimary;

  String expiryDate;

  private List<EdxActivationRoleEntity> edxActivationRoles;

}

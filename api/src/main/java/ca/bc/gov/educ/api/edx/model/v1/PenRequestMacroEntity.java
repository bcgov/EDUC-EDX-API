package ca.bc.gov.educ.api.edx.model.v1;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Table(name = "pen_retrieval_request_macro")
public class PenRequestMacroEntity {
  @Id
  @GeneratedValue(generator = "UUID")
  @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator", parameters = {
          @org.hibernate.annotations.Parameter(name = "uuid_gen_strategy_class", value = "org.hibernate.id.uuid.CustomVersionOneStrategy")})
  @Column(name = "pen_retrieval_request_macro_id", unique = true, updatable = false, columnDefinition = "BINARY(16)")
  UUID macroId;

  @Column(name = "MACRO_CODE")
  private String macroCode;

  @Column(name = "MACRO_TEXT")
  private String macroText;

  @Column(name = "MACRO_TYPE_CODE")
  private String macroTypeCode;

  @Column(name = "create_user", updatable = false)
  String createUser;

  @PastOrPresent
  @Column(name = "create_date", updatable = false)
  LocalDateTime createDate;

  @Column(name = "update_user")
  String updateUser;

  @PastOrPresent
  @Column(name = "update_date")
  LocalDateTime updateDate;
}

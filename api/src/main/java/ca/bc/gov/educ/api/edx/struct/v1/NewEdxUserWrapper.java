package ca.bc.gov.educ.api.edx.struct.v1;

import java.util.Optional;

import lombok.Getter;

/**
 * A wrapper for receiving new users who are assigned to schools that aren't
 * in the insititute service yet.
 */
@Getter
public class NewEdxUserWrapper {
  private EdxUser newUser;
  private Optional<School> newSchool;
}

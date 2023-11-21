package ca.bc.gov.educ.api.edx.struct.v1;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.opencsv.bean.CsvBindByPosition;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class OnboardingFileRow {

    @CsvBindByPosition(position = 0)
    private String mincode;

    @CsvBindByPosition(position = 1)
    private String schoolOrDistrictName;

    @CsvBindByPosition(position = 2)
    private String firstName;

    @CsvBindByPosition(position = 3)
    private String lastName;

    @CsvBindByPosition(position = 4)
    private String email;

}

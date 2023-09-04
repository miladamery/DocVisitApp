package ir.milad.DocVisitApp.infra.web;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestModel {
    @NotBlank(message = "first name is blank")
    public String firstName;
    @NotBlank(message = "last name is blank")
    public String lastName;
    public String phoneNumber = "";
    @NotBlank(message = "date Of Birth is blank")
    public String dateOfBirth;
    public Integer numOfPersons;

    {
        numOfPersons = 1;
    }
}

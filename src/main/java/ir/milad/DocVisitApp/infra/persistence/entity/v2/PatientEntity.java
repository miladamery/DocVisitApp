package ir.milad.DocVisitApp.infra.persistence.entity.v2;

import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Data;

import java.util.Objects;

@Data
public class PatientEntity {
    private final String phoneNumber;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;

    public static PatientEntity from(Patient patient) {
        return new PatientEntity(
                patient.getPhoneNumber(),
                patient.getFirstName(),
                patient.getLastName(),
                patient.getDateOfBirth()
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatientEntity that = (PatientEntity) o;
        return Objects.equals(firstName, that.firstName) && Objects.equals(lastName, that.lastName) && Objects.equals(dateOfBirth, that.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth);
    }
}

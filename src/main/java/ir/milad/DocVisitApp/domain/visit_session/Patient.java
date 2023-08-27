package ir.milad.DocVisitApp.domain.visit_session;

import lombok.Data;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

@Data
public class Patient {
    private final String phoneNumber;
    private final String firstName;
    private final String lastName;
    private final String dateOfBirth;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Patient patient = (Patient) o;
        return Objects.equals(firstName, patient.firstName) && Objects.equals(lastName, patient.lastName) && Objects.equals(dateOfBirth, patient.dateOfBirth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(firstName, lastName, dateOfBirth);
    }

    public int age() {
        return Period.between(LocalDate.parse(dateOfBirth), LocalDate.now()).getYears();
    }
}

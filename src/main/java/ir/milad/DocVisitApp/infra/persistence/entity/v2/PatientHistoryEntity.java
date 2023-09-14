package ir.milad.DocVisitApp.infra.persistence.entity.v2;

import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class PatientHistoryEntity {
    public LocalDate date;
    public PatientHistoryStatus status;

    public static PatientHistoryEntity from(PatientHistory patientHistory) {
        return new PatientHistoryEntity(
                patientHistory.date,
                patientHistory.status
        );
    }
}

package ir.milad.DocVisitApp.domain.patient;

import lombok.AllArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
public class PatientHistory {
    public LocalDate date;
    public PatientHistoryStatus status;
}

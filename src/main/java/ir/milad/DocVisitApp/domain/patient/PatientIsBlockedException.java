package ir.milad.DocVisitApp.domain.patient;

public class PatientIsBlockedException extends RuntimeException {

    public PatientIsBlockedException(String message) {
        super(message);
    }
}

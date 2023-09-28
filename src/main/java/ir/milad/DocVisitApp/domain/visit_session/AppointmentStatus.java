package ir.milad.DocVisitApp.domain.visit_session;

public enum AppointmentStatus {
    WAITING(true),
    VISITING(true),
    VISITED(false),
    CANCELED(false),
    CANCELED_BY_DOCTOR(false),
    ON_HOLD(true);

    public final boolean isActive;

    AppointmentStatus(boolean isActive) {
        this.isActive = isActive;
    }
}

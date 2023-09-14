package ir.milad.DocVisitApp.domain.visit_session;

import ir.milad.DocVisitApp.domain.patient.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitSessionRepository {
    void setActiveVisitSession(VisitSession visitSession);

    Optional<VisitSession> getActiveSession(LocalDateTime dateTime);

    Optional<VisitSession> getActiveSession(LocalDate date);

    void clearActiveVisitSession();

    void updateActiveVisitSession(VisitSession visitSession);

    List<VisitSession> getVisitSessionHistories();

    boolean hasHistory(Patient patient);

    default Optional<VisitSession> findActiveSessionForToday() {
        return getActiveSession(LocalDate.now());
    }

    default Optional<VisitSession> findActiveSessionForTodayAndNow() {
        return getActiveSession(LocalDateTime.now());
    }
}

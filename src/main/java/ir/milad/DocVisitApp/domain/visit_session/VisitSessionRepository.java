package ir.milad.DocVisitApp.domain.visit_session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface VisitSessionRepository {
    void setActiveVisitSession(VisitSession visitSession);
    Optional<VisitSession> getActiveSession(LocalDateTime dateTime);

    Optional<VisitSession> getActiveSession(LocalDate date);

    boolean exists(LocalDate date);
    void updateActiveVisitSession();
    default Optional<VisitSession> findActiveSessionForTodayAndNow() {
        return getActiveSession(LocalDateTime.now());
    }
}

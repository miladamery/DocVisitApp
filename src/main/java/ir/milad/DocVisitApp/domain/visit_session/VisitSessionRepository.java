package ir.milad.DocVisitApp.domain.visit_session;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface VisitSessionRepository {
    void setActiveVisitSession(VisitSession visitSession);
    Optional<VisitSession> getActiveSession(LocalDateTime dateTime);

    Optional<VisitSession> getActiveSession(LocalDate date);

    boolean exists(LocalDate date);
    void updateActiveVisitSession();

    List<VisitSession> getVisitSessionHistories();

    default Optional<VisitSession> findActiveSessionForToday() {
        return getActiveSession(LocalDate.now());
    }

    default Optional<VisitSession> findActiveSessionForTodayAndNow() {
        return getActiveSession(LocalDateTime.now());
    }
}

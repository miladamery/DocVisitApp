package ir.milad.DocVisitApp.domain.visit_session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

public interface VisitSessionRepository {
    void save(VisitSession visitSession);

    Optional<VisitSession> findById(UUID id);

    Optional<VisitSession> findByDateAndFromTimeAndToTime(LocalDate date, LocalTime fromTime, LocalTime toTime);

    Optional<VisitSession> findForTodayAndNow();

    void update();
}

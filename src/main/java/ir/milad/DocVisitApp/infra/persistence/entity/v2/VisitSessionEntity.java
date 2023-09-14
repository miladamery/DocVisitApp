package ir.milad.DocVisitApp.infra.persistence.entity.v2;

import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class VisitSessionEntity {
    private final String id;
    private LocalDate date;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private Integer sessionLength;
    private LocalDateTime lastAppointmentTime;
    private final List<AppointmentEntity> appointments;
    private Map<String, Long> onHoldTimes;

    public static VisitSessionEntity from(VisitSession visitSession) {
        return new VisitSessionEntity(
                visitSession.getId(),
                visitSession.getDate(),
                visitSession.getFromTime(),
                visitSession.getToTime(),
                visitSession.getSessionLength(),
                visitSession.getLastAppointmentTime(),
                visitSession.getAppointments().map(AppointmentEntity::from).toList(),
                visitSession.getOnHoldTimes()
        );
    }
}

package ir.milad.DocVisitApp.domain.visit_session;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VisitSessionSummary {
    public final long total;
    public final long waiting;
    public final long visited;
    public final long cancelled;
    public final String nextAppointmentId;
    public final String fromTime;
    public final String toTime;
    public final Integer sessionLength;

    public VisitSessionSummary(
            long total,
            long waiting,
            long visited,
            long cancelled,
            String nextAppointmentId,
            LocalDateTime fromTime,
            LocalDateTime toTime,
            Integer sessionLength) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        this.total = total;
        this.waiting = waiting;
        this.visited = visited;
        this.cancelled = cancelled;
        this.nextAppointmentId = nextAppointmentId;
        this.fromTime = fromTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        this.toTime = toTime.format(DateTimeFormatter.ofPattern("hh:mm a"));
        this.sessionLength = sessionLength;
    }
}

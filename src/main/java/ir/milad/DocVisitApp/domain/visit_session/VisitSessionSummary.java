package ir.milad.DocVisitApp.domain.visit_session;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class VisitSessionSummary {
    public final long total;
    public final long waiting;
    public final long visited;
    public final long cancelled;
    public final long expired;
    public final String nextAppointmentId;
}

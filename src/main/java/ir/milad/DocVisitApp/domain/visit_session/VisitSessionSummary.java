package ir.milad.DocVisitApp.domain.visit_session;

public class VisitSessionSummary {
    public final long total;
    public final long waiting;
    public final long visited;
    public final long cancelled;
    public final long expired;

    public VisitSessionSummary(long total, long waiting, long visited, long cancelled, long expired) {
        this.total = total;
        this.waiting = waiting;
        this.visited = visited;
        this.cancelled = cancelled;
        this.expired = expired;
    }
}

package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Service;

@Service
public class LoadDashboardDataService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadDashboardDataService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public DashboardData load() {
        var vs = visitSessionRepository.findActiveSessionForToday();
        if (vs.isEmpty())
            return new DashboardData(0L, "No Session is set");

        var officeHours = vs.get().getFromTime().toLocalTime() + " - " + vs.get().getToTime().toLocalTime();
        var numberOfPeopleAwaiting = vs.get().numberOfAppointmentsAwaiting();
        return new DashboardData(numberOfPeopleAwaiting, officeHours);
    }

    @Data
    @AllArgsConstructor
    public static class DashboardData {
        private Long numberOfPeopleAwaiting;
        private String officeHours;
    }
}



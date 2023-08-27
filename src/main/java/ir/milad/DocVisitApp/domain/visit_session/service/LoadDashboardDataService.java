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
        var vs = visitSessionRepository.findActiveSessionForTodayAndNow();
        if (vs.isEmpty())
            return new DashboardData(0L, "No Session is set");

        var officeHours = vs.get().getFromTime() + " " + vs.get().getToTime();
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



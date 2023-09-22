package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@UnitTestRequired
@Service
public class LoadDashboardDataService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadDashboardDataService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public DashboardData load() {
        var vs = visitSessionRepository.findActiveSessionForToday();
        if (vs.isEmpty())
            return new DashboardData(0L, "No Session is set", "-----------------");

        var officeHours = vs.get().getFromTime().toLocalTime() + " - " + vs.get().getToTime().toLocalTime();
        var numberOfPeopleAwaiting = vs.get().numberOfAppointmentsAwaiting();
        return new DashboardData(numberOfPeopleAwaiting, officeHours, vs.get().getSessionLength().toString());
    }

    @Data
    public static class DashboardData {
        private final Long numberOfPeopleAwaiting;
        private final String officeHours;
        private final String currentTime;
        private final String amPm;
        private final String currentWeekDay;
        private final String sessionLength;
        private final String day;
        private final String month;
        private final String year;

        public DashboardData(Long numberOfPeopleAwaiting, String officeHours, String sessionLength) {
            var today = LocalDate.now();
            var now = LocalTime.now();
            this.numberOfPeopleAwaiting = numberOfPeopleAwaiting;
            this.officeHours = officeHours;
            this.sessionLength = sessionLength;
            var ct = now.format(DateTimeFormatter.ofPattern("hh:mm a")).split(" ")[0];
            currentTime = ct.setCharBeforeAndAfter(' ', ct.indexOf(":"));
            amPm = now.format(DateTimeFormatter.ofPattern("hh:mm a")).split(" ")[1];
            currentWeekDay = today.getDayOfWeek().name();
            day = today.format(DateTimeFormatter.ofPattern("dd"));
            month = today.format(DateTimeFormatter.ofPattern("MMMM"));
            year = today.format(DateTimeFormatter.ofPattern("yyyy"));
        }
    }
}



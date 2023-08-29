package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionSummary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LoadPatientsDataService {

    private static final int PAGE_SIZE = 8;

    private final VisitSessionRepository visitSessionRepository;

    public LoadPatientsDataService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public PatientsData load(int pageNum) {
        var vs = visitSessionRepository.findActiveSessionForTodayAndNow();
        if (vs.isEmpty())
            throw new ApplicationException("No active session for seeing patients data.");

        var visitSession = vs.get();
        var appointments = visitSession
                .getAppointments()
                .subList((pageNum - 1) * PAGE_SIZE, Math.min(pageNum * PAGE_SIZE, visitSession.getAppointments().size()));
        return new PatientsData(visitSession.summary(), appointments, pageNum, PAGE_SIZE);
    }

    public static class PatientsData {
        public final VisitSessionSummary summary;
        public final String day;
        public final String month;
        public final String year;
        public final Integer pageNum;
        public final Integer pageSize;

        public final List<Appointment> appointments;
        public PatientsData(VisitSessionSummary summary, List<Appointment> appointments, Integer pageNum, Integer pageSize) {
            this.summary = summary;
            var today = LocalDate.now();
            day = today.format(DateTimeFormatter.ofPattern("dd"));
            month = today.format(DateTimeFormatter.ofPattern("MMMM"));
            year = today.format(DateTimeFormatter.ofPattern("yyyy"));
            this.appointments = appointments;
            this.pageNum = pageNum;
            this.pageSize = pageSize;
        }

        public String pagination() {
            if (pageNum == 1) {
                return " 1 - " + appointments.size();
            } else {
                var a = (pageNum - 1) * pageSize;
                var b = a + appointments.size();
                return (a + 1) + " - " + b + " of " + summary.total;
            }
        }

        public boolean hasNextPage() {
            var pagesCount = BigDecimal.valueOf(summary.total)
                    .divide(BigDecimal.valueOf(pageSize), RoundingMode.UP)
                    .longValue();
            return pageNum < pagesCount;
        }

        public boolean hasPrevious() {
            if (pageNum == 1)
                return false;

            return true;
        }
    }
}

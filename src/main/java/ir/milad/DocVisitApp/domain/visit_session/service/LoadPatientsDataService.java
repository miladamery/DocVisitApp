package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionSummary;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class LoadPatientsDataService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadPatientsDataService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public PatientsData load() {
        var vs = visitSessionRepository.findActiveSessionForToday();
        if (vs.isEmpty())
            throw new ApplicationException("No active session for seeing patients data.");

        var visitSession = vs.get();
        var appointments = visitSession
                .getAppointments();
        return new PatientsData(visitSession.summary(), appointments);
    }

    public static class PatientsData {
        public final VisitSessionSummary summary;
        public final String day;
        public final String month;
        public final String year;

        public final List<Appointment> appointments;
        public PatientsData(VisitSessionSummary summary, List<Appointment> appointments) {
            this.summary = summary;
            var today = LocalDate.now();
            day = today.format(DateTimeFormatter.ofPattern("dd"));
            month = today.format(DateTimeFormatter.ofPattern("MMMM"));
            year = today.format(DateTimeFormatter.ofPattern("yyyy"));
            this.appointments = appointments;
        }

        public boolean isVisiting() {
            return appointments.stream().anyMatch(ap -> ap.getStatus() == AppointmentStatus.VISITING);
        }
    }
}

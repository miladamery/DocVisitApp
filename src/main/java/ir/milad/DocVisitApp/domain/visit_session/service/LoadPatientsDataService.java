package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@UnitTestRequired
@Service
public class LoadPatientsDataService {

    private final VisitSessionRepository visitSessionRepository;

    public LoadPatientsDataService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public PatientsData load(LocalDate date) {
        Optional<VisitSession> visitSession;
        if (date == LocalDate.now())
            visitSession = visitSessionRepository.findActiveSessionForToday();
        else
            visitSession = visitSessionRepository.findVisitSessionByDate(date);
        return new PatientsData(visitSession, visitSessionRepository, date);
    }

    public static class PatientsData {
        public final VisitSessionSummary summary;
        public final String day;
        public final String month;
        public final String year;
        public final List<AppointmentDto> appointments;
        public final String currentTime;
        public final String currentWeekDay;
        public final String date;

        public PatientsData(Optional<VisitSession> visitSession, VisitSessionRepository visitSessionRepository, LocalDate date) {
            if (visitSession.isPresent()) {
                var vs = visitSession.get();
                this.summary = vs.summary();
                this.appointments = vs.getAppointments()
                        .map(appointment -> new AppointmentDto(appointment, visitSessionRepository))
                        .toList();
                this.date = summary.date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            } else {
                this.summary = null;
                this.appointments = new ArrayList<>();
                this.date = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            var today = LocalDate.now();
            var now = LocalTime.now();
            day = today.format(DateTimeFormatter.ofPattern("dd"));
            month = today.toFrenchMonth();
            year = today.format(DateTimeFormatter.ofPattern("yyyy"));
            var ct = now.format(DateTimeFormatter.ofPattern("HH:mm"));
            currentTime = ct.setCharBeforeAndAfter(' ', ct.indexOf(":"));
            currentWeekDay = StringUtils.capitalize(today.getDayOfWeek().toFrench().name().toLowerCase());
        }

        public boolean isVisiting() {
            return appointments.stream().anyMatch(ap -> ap.status == AppointmentStatus.VISITING);
        }
    }

    public static class AppointmentDto {
        public final String id;
        public final int turnNumber;
        public final int turnsToAwait;
        public final LocalDateTime visitTime;
        public final PatientDto patient;
        public final AppointmentStatus status;
        public final Integer numOfPersons;
        public final Boolean isActive;

        public AppointmentDto(Appointment appointment, VisitSessionRepository visitSessionRepository) {
            id = appointment.getId();
            turnNumber = appointment.getTurnNumber();
            turnsToAwait = appointment.getTurnsToAwait();
            visitTime = appointment.getVisitTime();
            patient = new PatientDto(appointment.getPatient(), visitSessionRepository.hasHistory(appointment.getPatient()));
            status = appointment.getStatus();
            numOfPersons = appointment.getNumOfPersons();
            isActive = appointment.getStatus().isActive;
        }
    }

    public static class PatientDto {
        public final String phoneNumber;
        public final String firstName;
        public final String lastName;
        public final String dateOfBirth;
        public final Boolean hasHistory;

        public final int age;
        public PatientDto(Patient patient, Boolean hasHistory) {
            this.firstName = patient.getFirstName();
            this.lastName = patient.getLastName();
            this.dateOfBirth = patient.getDateOfBirth();
            this.phoneNumber = patient.getPhoneNumber();
            this.hasHistory = hasHistory;
            this.age = patient.age();
        }
    }
}

package ir.milad.DocVisitApp.domain.visit_session.service;

import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@UnitTestRequired
@Service
public class DoctorGivingAppointmentService {

    private final VisitSessionRepository visitSessionRepository;

    public DoctorGivingAppointmentService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    public synchronized Appointment giveAppointment(Patient patient, LocalTime entryTime) {
        var vs = visitSessionRepository.findActiveSessionForTodayAndNow()
                .orElseThrow(() -> new ApplicationException("Active session not found."));

        LocalTime realEntryTime;
        if (isDocGivingAppointmentBeforeSessionStart(entryTime, vs))
            realEntryTime = vs.getLastAppointmentTime().toLocalTime();
        else
            realEntryTime = entryTime;

        var turn = vs.giveAppointment(patient, realEntryTime.withSecond(0).withNano(0), 1);
        visitSessionRepository.updateActiveVisitSession(vs);

        return turn;
    }

    private static boolean isDocGivingAppointmentBeforeSessionStart(LocalTime entryTime, VisitSession vs) {
        return entryTime.isBefore(vs.getFromTime().toLocalTime());
    }
}

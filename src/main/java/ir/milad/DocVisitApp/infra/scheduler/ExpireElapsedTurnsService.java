package ir.milad.DocVisitApp.infra.scheduler;

import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryStatus;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
public class ExpireElapsedTurnsService {

    private final VisitSessionRepository visitSessionRepository;
    private final PatientRepository patientRepository;

    public ExpireElapsedTurnsService(VisitSessionRepository visitSessionRepository, PatientRepository patientRepository) {
        this.visitSessionRepository = visitSessionRepository;
        this.patientRepository = patientRepository;
    }

    @Scheduled(initialDelay = 10000, fixedDelay = 1000)
    public void expire() {
        var vs = visitSessionRepository.findActiveSessionForTodayAndNow();
        if (vs.isPresent()) {
            var visitSession = vs.get();
            visitSession.getAppointments().forEach(appointment -> {
                if (
                        appointment.getVisitTime().plusMinutes(visitSession.getSessionLength()).isBefore(LocalTime.now()) &&
                                appointment.getStatus() == AppointmentStatus.WAITING
                ) {
                    appointment.setStatus(AppointmentStatus.EXPIRED);
                    patientRepository.addPatientHistory(appointment.getPatient(), new PatientHistory(LocalDate.now(), PatientHistoryStatus.EXPIRED));
                }
            });
            visitSessionRepository.updateActiveVisitSession();
        }
    }
}

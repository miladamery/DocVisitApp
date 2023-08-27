package ir.milad.DocVisitApp.infra.scheduler;

import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class ExpireElapsedTurnsService {

    private final VisitSessionRepository visitSessionRepository;

    public ExpireElapsedTurnsService(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    @Scheduled(fixedDelay = 1000)
    public void expire() {
        var vs = visitSessionRepository.findForTodayAndNow();
        if (vs.isPresent()) {
            var visitSession = vs.get();
            visitSession.getAppointments().forEach(appointment -> {
                if (appointment.getVisitTime().plusMinutes(visitSession.getSessionLength()).isBefore(LocalTime.now()))
                    appointment.setStatus(AppointmentStatus.EXPIRED);
            });
            visitSessionRepository.update();
        }
    }
}

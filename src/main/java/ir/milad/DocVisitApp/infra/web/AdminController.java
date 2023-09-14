package ir.milad.DocVisitApp.infra.web;

import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final VisitSessionRepository visitSessionRepository;

    public AdminController(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    @PutMapping("/set/visit/time")
    @ResponseBody
    public void setVisitTime(@RequestBody Request request) {
        var vs = visitSessionRepository.findActiveSessionForToday().get();
        vs.getAppointments()
                .stream().filter(appointment -> appointment.getTurnNumber() == request.ticketNum)
                .findFirst().get().setVisitTime(LocalDateTime.of(LocalDate.now(), request.time));
        visitSessionRepository.updateActiveVisitSession(vs);
    }

    @PutMapping("/set/last/appointment/time")
    @ResponseBody
    public void setLastAppointmentTime(@RequestBody Request2 request) {
        var vs = visitSessionRepository.findActiveSessionForToday().get();
        vs.setLastAppointmentTime(LocalDateTime.of(LocalDate.now(), request.time));
        visitSessionRepository.updateActiveVisitSession(vs);
    }

    @DeleteMapping("/clear/active/visit/session")
    @ResponseBody
    public void clearActiveVisitSession() {
        visitSessionRepository.clearActiveVisitSession();
    }

    public static class Request {
        public Integer ticketNum;
        public LocalTime time;
    }

    public static class Request2 {
        public LocalTime time;
    }
}

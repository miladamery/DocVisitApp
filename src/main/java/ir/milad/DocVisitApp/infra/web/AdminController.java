package ir.milad.DocVisitApp.infra.web;

import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private VisitSessionRepository visitSessionRepository;

    public AdminController(VisitSessionRepository visitSessionRepository) {
        this.visitSessionRepository = visitSessionRepository;
    }

    @PutMapping("/set/visit/time")
    @ResponseBody
    public void setVisitTime(@RequestBody Request request) {
        visitSessionRepository
                .findActiveSessionForToday()
                .get()
                .getAppointments()
                .stream().filter(appointment -> appointment.getTurnNumber() == request.ticketNum)
                .findFirst().get().setVisitTime(LocalDateTime.of(LocalDate.now(), request.time));
    }

    public static class Request {
        public Integer ticketNum;
        public LocalTime time;
    }
}

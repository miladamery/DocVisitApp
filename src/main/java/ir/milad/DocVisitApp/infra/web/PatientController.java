package ir.milad.DocVisitApp.infra.web;

import io.vavr.control.Try;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.service.TakeAppointmentService;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.service.CancelAppointmentService;
import ir.milad.DocVisitApp.domain.visit_session.service.GetActiveSessionService;
import ir.milad.DocVisitApp.domain.visit_session.service.LoadAppointmentService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalTime;

@Controller
@RequestMapping("/patient")
public class PatientController {

    public static final String HTMX_REDIRECT_HEADER = "HX-Redirect";
    public static final String ERROR_500_ATTRIBUTE_NAME = "errorMessage";

    private final TakeAppointmentService takeAppointmentService;
    private final GetActiveSessionService getActiveSessionService;
    private final LoadAppointmentService loadAppointmentService;
    private final CancelAppointmentService cancelAppointmentService;

    public PatientController(
            TakeAppointmentService takeAppointmentService,
            GetActiveSessionService getActiveSessionService,
            LoadAppointmentService loadAppointmentService,
            CancelAppointmentService cancelAppointmentService) {
        this.takeAppointmentService = takeAppointmentService;
        this.getActiveSessionService = getActiveSessionService;
        this.loadAppointmentService = loadAppointmentService;
        this.cancelAppointmentService = cancelAppointmentService;
    }

    @GetMapping("/index")
    public String index() {
        return getActiveSessionService.findActiveSessionForTodayAndNow()
                .map(__ -> "patient/index")
                .orElse("patient/no-active-visit-session");
    }

    @PostMapping(value = "/get/appointment")
    public String getTurn(@Valid @RequestBody PatientRequestModel request, Model model) {
        return Try.of(() -> {
                    var patient = getPatientFromRequest(request);
                    return takeAppointmentService.takeAppointment(patient, LocalTime.now().withSecond(0).withNano(0));
                })
                .map(appointment -> appointmentInfo(model, appointment))
                .recover(throwable -> {
                    if (throwable instanceof ApplicationException) {
                        return "patient/no-active-visit-session";
                    } else {
                        model.addAttribute(ERROR_500_ATTRIBUTE_NAME, throwable.getMessage());
                        return "500";
                    }
                })
                .get();
    }

    @GetMapping("/load/appointment")
    public String loadTurn(@RequestParam String id, Model model) {
        return loadAppointmentService.loadTurn(id)
                .map(appointment -> {
                    if (appointment.getStatus() == AppointmentStatus.VISITING) {
                        model.addAttribute("turnNumber", appointment.getTurnNumber());
                        return "patient/appointment-arrived.html :: appointment-arrived";
                    } else
                        return appointmentInfo(model, appointment);
                })
                .orElseGet(() -> {
                    model.addAttribute(ERROR_500_ATTRIBUTE_NAME, "Your turn not found");
                    return "500";
                });
    }

    @DeleteMapping("/cancel/appointment")
    public String cancelAppointment(@RequestParam String id, HttpServletResponse response, Model model) {
        return Try.run(() -> cancelAppointmentService.cancel(id))
                .map(__ -> {
                    response.setHeader(HTMX_REDIRECT_HEADER, "/patient/index");
                    return "";
                })
                .recover(throwable -> {
                    model.addAttribute(ERROR_500_ATTRIBUTE_NAME, throwable.getMessage());
                    return "500";
                })
                .get();
    }

    private Patient getPatientFromRequest(PatientRequestModel request) {
        return new Patient(
                request.phoneNumber.trim(),
                request.firstName.trim(),
                request.lastName.trim(),
                request.dateOfBirth.trim()
        );
    }

    private String appointmentInfo(Model model, Appointment appointment) {
        model.addAttribute("appointment", appointment);
        model.addAttribute("waitingTime", appointment.waitingTimeFromNow());
        return "patient/appointment-info :: appointment";
    }

}

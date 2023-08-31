package ir.milad.DocVisitApp.infra.web;

import io.vavr.control.Try;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientIsBlockedException;
import ir.milad.DocVisitApp.domain.patient.service.*;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.service.GetActiveVisitSessionService;
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
    private final GetActiveVisitSessionService getActiveVisitSessionService;
    private final LoadPatientAppointmentService loadPatientAppointmentService;
    private final CancelPatientAppointmentService cancelPatientAppointmentService;
    private final LoadPatientHistoryService loadPatientHistoryService;
    private final BlockPatientService blockPatientService;
    private final PatientAppointmentCheckInService patientAppointmentCheckInService;
    private final PatientAppointmentDoneService patientAppointmentDoneService;
    public PatientController(
            TakeAppointmentService takeAppointmentService,
            GetActiveVisitSessionService getActiveVisitSessionService,
            LoadPatientAppointmentService loadPatientAppointmentService,
            CancelPatientAppointmentService cancelPatientAppointmentService,
            LoadPatientHistoryService loadPatientHistoryService, BlockPatientService blockPatientService, PatientAppointmentCheckInService patientAppointmentCheckInService, PatientAppointmentDoneService patientAppointmentDoneService) {
        this.takeAppointmentService = takeAppointmentService;
        this.getActiveVisitSessionService = getActiveVisitSessionService;
        this.loadPatientAppointmentService = loadPatientAppointmentService;
        this.cancelPatientAppointmentService = cancelPatientAppointmentService;
        this.loadPatientHistoryService = loadPatientHistoryService;
        this.blockPatientService = blockPatientService;
        this.patientAppointmentCheckInService = patientAppointmentCheckInService;
        this.patientAppointmentDoneService = patientAppointmentDoneService;
    }

    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "fr") String language, Model model) {
        model.addAttribute("language", language);
        return getActiveVisitSessionService.findActiveSessionForTodayAndNow()
                .map(__ -> "patient/index")
                .orElse("patient/no-active-visit-session");
    }

    @PostMapping(value = "/get/appointment")
    public String getAppointment(@Valid @RequestBody PatientRequestModel request, Model model) {
        return Try.of(() -> {
                    var patient = getPatientFromRequest(request);
                    return takeAppointmentService.takeAppointment(patient, LocalTime.now().withSecond(0).withNano(0));
                })
                .map(appointment -> appointmentInfo(model, appointment))
                .recover(throwable -> {
                    if (throwable instanceof ApplicationException) {
                        return "patient/no-active-visit-session";
                    } else if (throwable instanceof PatientIsBlockedException) {
                        return "patient/blocked-by-doctor";
                    } else {
                        model.addAttribute(ERROR_500_ATTRIBUTE_NAME, throwable.getMessage());
                        return "500";
                    }
                })
                .get();
    }

    @GetMapping("/load/appointment")
    public String loadTurn(@RequestParam(defaultValue = "fr") String language, @RequestParam String id, Model model) {
        model.addAttribute("language", language);
        return loadPatientAppointmentService.loadPatientAppointment(id)
                .map(appointment -> {
                    if (appointment.status == AppointmentStatus.VISITING) {
                        model.addAttribute("turnNumber", appointment.turnNumber);
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
    public String cancelAppointment(
            @RequestParam String id, HttpServletResponse response,
            Model model) {
        return Try.run(() -> cancelPatientAppointmentService.cancel(id))
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

    @GetMapping("/load/history")
    public String loadPatientHistory(
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String dateOfBirth,
            Model model
    ) {
        var patient = new Patient("", firstName, lastName, dateOfBirth);
        model.addAttribute("patient", patient);
        model.addAttribute("histories", loadPatientHistoryService.load(patient));
        return "/doctor/patients-history :: patient-history";
    }

    @PutMapping("/block")
    @ResponseBody
    public void block(@Valid @RequestBody PatientRequestModel request) {
        blockPatientService.block(getPatientFromRequest(request));
    }

    @PutMapping("/check/in/{id}")
    @ResponseBody
    public void checkIn(@PathVariable String id) {
        patientAppointmentCheckInService.checkIn(id);
    }

    @PutMapping("/done/{id}")
    @ResponseBody
    public void done(@PathVariable String id) {
        patientAppointmentDoneService.done(id);
    }

    private Patient getPatientFromRequest(PatientRequestModel request) {
        return new Patient(
                request.phoneNumber.trim(),
                request.firstName.trim(),
                request.lastName.trim(),
                request.dateOfBirth.trim()
        );
    }

    private String appointmentInfo(Model model, AppointmentData appointment) {
        model.addAttribute("appointment", appointment);
        model.addAttribute("waitingTime", appointment.waitingTime);
        return "patient/appointment-info :: appointment";
    }

}

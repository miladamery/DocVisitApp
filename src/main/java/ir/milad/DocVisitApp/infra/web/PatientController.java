package ir.milad.DocVisitApp.infra.web;

import io.vavr.control.Try;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientIsBlockedException;
import ir.milad.DocVisitApp.domain.patient.service.*;
import ir.milad.DocVisitApp.domain.visit_session.AppointmentStatus;
import ir.milad.DocVisitApp.domain.visit_session.service.GetActiveVisitSessionService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;

@Controller
@RequestMapping("/patient")
@Slf4j
public class PatientController {

    public static final String HTMX_REDIRECT_HEADER = "HX-Redirect";
    public static final String ERROR_500_ATTRIBUTE_NAME = "errorMessage";
    public static final String COOKIE_NAME = "patient_info";

    private final TakeAppointmentService takeAppointmentService;
    private final GetActiveVisitSessionService getActiveVisitSessionService;
    private final LoadPatientAppointmentService loadPatientAppointmentService;
    private final CancelPatientAppointmentService cancelPatientAppointmentService;
    private final LoadPatientHistoryService loadPatientHistoryService;
    private final BlockPatientService blockPatientService;
    private final PatientAppointmentCheckInService patientAppointmentCheckInService;
    private final PatientAppointmentDoneService patientAppointmentDoneService;
    private final PatientAppointmentOnHoldService patientAppointmentOnHoldService;
    private final PatientAppointmentResumeService patientAppointmentResumeService;

    public PatientController(
            TakeAppointmentService takeAppointmentService,
            GetActiveVisitSessionService getActiveVisitSessionService,
            LoadPatientAppointmentService loadPatientAppointmentService,
            CancelPatientAppointmentService cancelPatientAppointmentService,
            LoadPatientHistoryService loadPatientHistoryService,
            BlockPatientService blockPatientService,
            PatientAppointmentCheckInService patientAppointmentCheckInService,
            PatientAppointmentDoneService patientAppointmentDoneService, PatientAppointmentOnHoldService patientAppointmentOnHoldService, PatientAppointmentResumeService patientAppointmentResumeService) {
        this.takeAppointmentService = takeAppointmentService;
        this.getActiveVisitSessionService = getActiveVisitSessionService;
        this.loadPatientAppointmentService = loadPatientAppointmentService;
        this.cancelPatientAppointmentService = cancelPatientAppointmentService;
        this.loadPatientHistoryService = loadPatientHistoryService;
        this.blockPatientService = blockPatientService;
        this.patientAppointmentCheckInService = patientAppointmentCheckInService;
        this.patientAppointmentDoneService = patientAppointmentDoneService;
        this.patientAppointmentOnHoldService = patientAppointmentOnHoldService;
        this.patientAppointmentResumeService = patientAppointmentResumeService;
    }

    @GetMapping("/index")
    public String index(@RequestParam(defaultValue = "fr") String language, Model model) {
        model.addAttribute("language", language);
        model.addAttribute("cookieName", COOKIE_NAME);
        return getActiveVisitSessionService.findActiveSessionForTodayAndNow()
                .map(vs -> "patient/index")
                .orElse("patient/office-closed");
    }

    @GetMapping("/form")
    public String form(@RequestParam(defaultValue = "fr") String language, Model model) {
        model.addAttribute("language", language);
        return "patient/appointment-form :: appointment-form";
    }

    @PostMapping(value = "/get/appointment")
    public String getAppointment(
            @Valid @RequestBody PatientRequestModel request,
            @RequestParam(defaultValue = "fr") String language,
            Model model,
            HttpServletResponse response) {
        model.addAttribute("language", language);
        return Try.of(() -> {
                    var patient = getPatientFromRequest(request);
                    var entryTime = LocalTime.now().withSecond(0).withNano(0);
                    return takeAppointmentService.takeAppointment(patient, entryTime, request.numOfPersons);
                })
                .map(appointment -> {
                    response.addCookie(
                            Cookie.of(
                                    COOKIE_NAME,
                                    URLEncoder.encode(appointment.id, StandardCharsets.UTF_8),
                                    3 * 60 * 60
                            )
                    );
                    return appointmentInfo(model, appointment);
                })
                .recover(throwable -> {
                    if (throwable instanceof ApplicationException) {
                        return "patient/office-closed :: office-closed";
                    } else if (throwable instanceof PatientIsBlockedException) {
                        return "patient/blocked :: blocked";
                    } else {
                        model.addAttribute(ERROR_500_ATTRIBUTE_NAME, throwable.getMessage());
                        return "500";
                    }
                })
                .get();
    }

    @GetMapping("/load/appointment")
    public String loadAppointment(
            @RequestParam(defaultValue = "fr") String language,
            @RequestParam String id,
            @RequestParam(defaultValue = "false") Boolean isAppointmentSet,
            Model model,
            HttpServletResponse response
    ) {
        if (isAppointmentSet)
            log.info("Event: Loading appointment#{} from cookie", id);
        model.addAttribute("language", language);
        return loadPatientAppointmentService.loadPatientAppointment(id)
                .map(appointment -> {
                    if (isAppointmentSet)
                        log.info("Event: Appointment#{} loaded from cookie details is: {}", id, appointment.toString());
                    if (appointment.status == AppointmentStatus.VISITING) {
                        model.addAttribute("turnNumber", appointment.turnNumber);
                        model.addAttribute("appointmentId", id);
                        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
                        return "patient/appointment-arrived :: appointment-arrived";
                    } else if (appointment.status == AppointmentStatus.CANCELED_BY_DOCTOR) {
                        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
                        return "patient/appointment-cancel-by-doc :: appointment-cancel-by-doc";
                    }
                    else if (appointment.status == AppointmentStatus.VISITED) {
                        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
                        return "patient/appointment-done :: apponintment-done";
                    }
                    else
                        return appointmentInfo(model, appointment);

                })
                .orElseGet(() -> {
                    response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
                    model.addAttribute(ERROR_500_ATTRIBUTE_NAME, "Your turn not found");
                    return "500";
                });
    }

    @DeleteMapping("/cancel/appointment")
    public String cancelAppointment(@RequestParam String id, HttpServletResponse response, Model model) {
        return Try.run(() -> cancelPatientAppointmentService.cancelByPatient(id))
                .map(__ -> {
                    response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
                    response.setHeader(HTMX_REDIRECT_HEADER, "/patient/index");
                    return "/patient/index";
                })
                .recover(throwable -> {
                    response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
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

    @PutMapping("/on-hold/{appointmentId}")
    @ResponseBody
    public void onHold(@PathVariable String appointmentId) {
        patientAppointmentOnHoldService.onHold(appointmentId);
    }

    @PutMapping("/resume/{appointmentId}")
    @ResponseBody
    public void resume(@PathVariable String appointmentId) {
        patientAppointmentResumeService.resume(appointmentId);
    }

    @GetMapping("/blocked")
    public String blocked(@RequestParam(defaultValue = "fr") String language, Model model, HttpServletResponse response) {
        model.addAttribute("language", language);
        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
        return "patient/blocked :: blocked";
    }

    @GetMapping("/closed")
    public String closed(@RequestParam(defaultValue = "fr") String language, Model model, HttpServletResponse response) {
        model.addAttribute("language", language);
        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
        return "patient/office-closed :: office-closed";
    }

    @GetMapping("/appointment/info")
    public String appointmentInfo(@RequestParam(defaultValue = "fr") String language, String id, Model model) {
        model.addAttribute("language", language);
        return appointmentInfo(model, loadPatientAppointmentService.loadPatientAppointment(id).get());
    }

    @GetMapping("/done")
    public String done(@RequestParam(defaultValue = "fr") String language, Model model, HttpServletResponse response) {
        model.addAttribute("language", language);
        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
        return "patient/appointment-done :: apponintment-done";
    }

    @GetMapping("/cancelled/by/doc")
    public String cancelledByDoc(@RequestParam(defaultValue = "fr") String language, Model model, HttpServletResponse response) {
        model.addAttribute("language", language);
        response.addCookie(Cookie.of(COOKIE_NAME, "", 0));
        return "patient/appointment-cancel-by-doc :: appointment-cancel-by-doc";
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
        return "patient/appointment-info :: appointment-info";
    }

}

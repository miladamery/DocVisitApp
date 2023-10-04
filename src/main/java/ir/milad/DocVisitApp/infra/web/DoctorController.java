package ir.milad.DocVisitApp.infra.web;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.service.CancelPatientAppointmentService;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import ir.milad.DocVisitApp.domain.visit_session.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UpsertVisitSessionService upsertVisitSessionService;
    private final LoadDashboardDataService loadDashboardDataService;
    private final LoadPatientsDataService loadPatientsDataService;
    private final DoctorGivingAppointmentService doctorGivingAppointmentService;
    private final VisitSessionRepository visitSessionRepository;
    private final CancelPatientAppointmentService cancelPatientAppointmentService;

    private final GetActiveVisitSessionService getActiveVisitSessionService;

    public DoctorController(
            UpsertVisitSessionService upsertVisitSessionService,
            LoadDashboardDataService loadDashboardDataService,
            LoadPatientsDataService loadPatientsDataService,
            DoctorGivingAppointmentService doctorGivingAppointmentService,
            VisitSessionRepository visitSessionRepository,
            CancelPatientAppointmentService cancelPatientAppointmentService,
            GetActiveVisitSessionService getActiveVisitSessionService) {
        this.upsertVisitSessionService = upsertVisitSessionService;
        this.loadDashboardDataService = loadDashboardDataService;
        this.loadPatientsDataService = loadPatientsDataService;
        this.doctorGivingAppointmentService = doctorGivingAppointmentService;
        this.visitSessionRepository = visitSessionRepository;
        this.cancelPatientAppointmentService = cancelPatientAppointmentService;
        this.getActiveVisitSessionService = getActiveVisitSessionService;
    }

    @PostMapping(value = "/create/visit_session")
    @ResponseBody
    public void createVisitSession(@Valid @RequestBody VisitSessionRequest request) {
        upsertVisitSessionService.upsert(
                request.date,
                request.fromTime,
                request.toTime,
                request.sessionLength
        );
    }

    @PostMapping("/give/appointment")
    @ResponseBody
    public void giveAppointment(@Valid @RequestBody PatientRequestModel request, Model model) {
        var patient = new Patient(request.phoneNumber, request.firstName, request.lastName, request.dateOfBirth);
        doctorGivingAppointmentService.giveAppointment(patient, LocalTime.now().withNano(0));
    }

    @GetMapping("/index")
    public String index(Model model) {
        loadDashboardInfo(model);
        return "/doctor/index";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        loadDashboardInfo(model);
        return "/doctor/dashboard :: dashboard";
    }

    @GetMapping("/patients")
    public String patients(Model model, @RequestParam(value = "visitSessionDate", required = false, defaultValue = "") String visitSessionDate) {
        LocalDate date;
        if (visitSessionDate == null || visitSessionDate.isBlank())
            date = LocalDate.now();
        else
            date = LocalDate.parse(visitSessionDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        var patientsData = loadPatientsDataService.load(date);
        model.addAttribute("data", patientsData);
        return "/doctor/patients :: patients";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        var today = LocalDate.now();
        LocalDate previousMonday;
        if (today.getDayOfWeek() == DayOfWeek.SUNDAY)
            previousMonday = today;
        else
            previousMonday = today.with(TemporalAdjusters.previous(DayOfWeek.SUNDAY));
        LocalDate nextMonday = today.with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        var activeVsOptional = getActiveVisitSessionService.findActiveVisitSessionForToday();
        var groupedVisitSessions = visitSessionRepository
                .getVisitSessionHistories()
                .filter(vs -> vs.getDate() >= previousMonday && vs.getDate() < nextMonday)
                .groupingBy(VisitSession::getDate);
        activeVsOptional.ifPresent(vs -> groupedVisitSessions.put(vs.getDate(), List.of(vs)));
        var visitSessions = groupedVisitSessions.values().mapToList(it -> {
            var indx = 0;
            var max = 0;
            for (int i = 0; i < it.size(); i++) {
                if (it.get(i).getAppointments().size() > max)
                    indx = i;
            }
            return it.get(indx);
        });
        var data = visitSessions.mapToList(vs -> VisitSessionCalendarViewData.of(
                vs.getFromTime().format(DateTimeFormatter.ISO_DATE_TIME),
                vs.getToTime().format(DateTimeFormatter.ISO_DATE_TIME),
                vs.getFromTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) + " - " +
                        vs.getToTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm"))
        ));

        var d = today.format(DateTimeFormatter.ofPattern("dd"));
        var m = today.toFrenchMonth();
        var y = today.format(DateTimeFormatter.ofPattern("yyyy"));
        model.addAttribute("d", d);
        model.addAttribute("m", m);
        model.addAttribute("y", y);
        model.addAttribute("today", LocalDate.now().toString().formatted(DateTimeFormatter.ofPattern("yyyyMMdd")));

        model.addAttribute("events", data);
        getActiveVisitSessionService
                .findActiveVisitSessionForToday()
                .ifPresentOrElse(avs -> {
                            model.addAttribute("hasPatient", !avs.getAppointments().isEmpty());
                            model.addAttribute("fromTime", avs.getFromTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                            model.addAttribute("toTime", avs.getToTime().format(DateTimeFormatter.ofPattern("HH:mm")));
                            model.addAttribute("sessionLength", avs.getSessionLength());
                        },
                        () -> {
                            model.addAttribute("hasPatient", false);
                            model.addAttribute("fromTime", "");
                            model.addAttribute("toTime", "");
                            model.addAttribute("sessionLength", 8);
                        }
                );
        return "/doctor/calendar.html :: calendar";
    }

    @DeleteMapping("/cancel/appointment/{appointmentId}")
    @ResponseBody
    public void cancelAppointment(@PathVariable String appointmentId) {
        cancelPatientAppointmentService.cancelByDoctor(appointmentId);
    }

    private void loadDashboardInfo(Model model) {
        var dashboardInfo = loadDashboardDataService.load();
        model.addAttribute("dashboardInfo", dashboardInfo);
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class VisitSessionRequest {
        @NotNull(message = "date cant be null")
        private LocalDate date;
        @NotNull(message = "fromTime cant be null")
        private LocalTime fromTime;
        @NotNull(message = "toTime cant be null")
        private LocalTime toTime;
        @Positive(message = "visit session length can't be lower than 0")
        @NotNull(message = "Please provide session length")
        private Integer sessionLength;
    }

    public static class DayOfMonthAndText {
        public Integer dayOfMonth;
        public String day;
        public Boolean isToday;

        public DayOfMonthAndText(Integer dayOfMonth, String day, Boolean isToday) {
            this.dayOfMonth = dayOfMonth;
            this.day = day;
            this.isToday = isToday;
        }
    }

    public static class TimeData {
        public String time;
        public List<Boolean> hasSessions;

        public TimeData(String time, List<Boolean> hasSessions) {
            this.time = time;
            this.hasSessions = hasSessions;
        }
    }

    @AllArgsConstructor(staticName = "of")
    static class VisitSessionCalendarViewData {
        public final String start;
        public final String end;
        public final String title;
    }
}

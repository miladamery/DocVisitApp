package ir.milad.DocVisitApp.infra.web;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.service.CancelPatientAppointmentService;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import ir.milad.DocVisitApp.domain.visit_session.service.DoctorGivingAppointmentService;
import ir.milad.DocVisitApp.domain.visit_session.service.LoadDashboardDataService;
import ir.milad.DocVisitApp.domain.visit_session.service.LoadPatientsDataService;
import ir.milad.DocVisitApp.domain.visit_session.service.UpsertVisitSessionService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
import java.util.Arrays;
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
    public DoctorController(
            UpsertVisitSessionService upsertVisitSessionService,
            LoadDashboardDataService loadDashboardDataService,
            LoadPatientsDataService loadPatientsDataService,
            DoctorGivingAppointmentService doctorGivingAppointmentService,
            VisitSessionRepository visitSessionRepository,
            CancelPatientAppointmentService cancelPatientAppointmentService) {
        this.upsertVisitSessionService = upsertVisitSessionService;
        this.loadDashboardDataService = loadDashboardDataService;
        this.loadPatientsDataService = loadPatientsDataService;
        this.doctorGivingAppointmentService = doctorGivingAppointmentService;
        this.visitSessionRepository = visitSessionRepository;
        this.cancelPatientAppointmentService = cancelPatientAppointmentService;
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
    public String patients(Model model) {
        var patientsData = loadPatientsDataService.load();
        model.addAttribute("data", patientsData);
        return "/doctor/patients :: patients";
    }

    @GetMapping("/calendar")
    public String calendar(Model model) {
        var today = LocalDate.now();
        LocalDate previousMonday;
        if (today.getDayOfWeek() == DayOfWeek.MONDAY)
            previousMonday = today;
        else
            previousMonday = today.with(TemporalAdjusters.previous(DayOfWeek.MONDAY));
        var times = Arrays.asList(
                "00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14",
                "15", "16", "17", "18", "19", "20", "21", "22", "23");

        var dates = Arrays.asList(
                previousMonday,
                previousMonday.plusDays(1),
                previousMonday.plusDays(2),
                previousMonday.plusDays(3),
                previousMonday.plusDays(4),
                previousMonday.plusDays(5),
                previousMonday.plusDays(6)
        );
        var ttimes = times.stream().map(t -> {
            var time = LocalTime.of(Integer.valueOf(t), 0);
            var res = dates.stream().map(ld ->
                    visitSessionRepository.getVisitSessionHistories().stream().anyMatch(vs ->
                            vs.getDate().equals(ld) &&
                                    ((vs.getFromTime().equals(time) || vs.getFromTime().toLocalTime().isBefore(time)) &&
                                            (vs.getToTime().toLocalTime().isAfter(time) || vs.getToTime().equals(time))
                                    )
                    )
            ).toList();
            return new TimeData(t, res);
        }).toList();
        var monday = new DayOfMonthAndText(
                previousMonday.getDayOfMonth(),
                previousMonday.getDayOfWeek().name(),
                previousMonday.equals(LocalDate.now())
        );
        var tuesday = new DayOfMonthAndText(
                previousMonday.plusDays(1).getDayOfMonth(),
                previousMonday.plusDays(1).getDayOfWeek().name().toLowerCase(),
                previousMonday.plusDays(1).equals(LocalDate.now())
        );
        var wednesday = new DayOfMonthAndText(
                previousMonday.plusDays(2).getDayOfMonth(),
                previousMonday.plusDays(2).getDayOfWeek().name().toLowerCase(),
                previousMonday.plusDays(2).equals(LocalDate.now())
        );
        var thursday = new DayOfMonthAndText(
                previousMonday.plusDays(3).getDayOfMonth(),
                previousMonday.plusDays(3).getDayOfWeek().name().toLowerCase(),
                previousMonday.plusDays(3).equals(LocalDate.now())
        );
        var friday = new DayOfMonthAndText(
                previousMonday.plusDays(4).getDayOfMonth(),
                previousMonday.plusDays(4).getDayOfWeek().name().toLowerCase(),
                previousMonday.plusDays(4).equals(LocalDate.now())
        );
        var saturday = new DayOfMonthAndText(
                previousMonday.plusDays(5).getDayOfMonth(),
                previousMonday.plusDays(5).getDayOfWeek().name().toLowerCase(),
                previousMonday.plusDays(5).equals(LocalDate.now())
        );
        var sunday = new DayOfMonthAndText(
                previousMonday.plusDays(6).getDayOfMonth(),
                previousMonday.plusDays(6).getDayOfWeek().name().toLowerCase(),
                previousMonday.plusDays(6).equals(LocalDate.now())
        );
        var days = Arrays.asList(monday, tuesday, wednesday, thursday, friday, saturday, sunday);

        var d = today.format(DateTimeFormatter.ofPattern("dd"));
        var m = today.format(DateTimeFormatter.ofPattern("MMMM"));
        var y = today.format(DateTimeFormatter.ofPattern("yyyy"));

        model.addAttribute("days", days);
        model.addAttribute("times", ttimes);
        model.addAttribute("d", d);
        model.addAttribute("m", m);
        model.addAttribute("y", y);
        model.addAttribute("today", LocalDate.now().toString().formatted(DateTimeFormatter.ofPattern("yyyyMMdd")));
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
}

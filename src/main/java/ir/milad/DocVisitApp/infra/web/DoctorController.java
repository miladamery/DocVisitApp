package ir.milad.DocVisitApp.infra.web;

import io.vavr.control.Try;
import ir.milad.DocVisitApp.domain.visit_session.service.CreateVisitSessionService;
import ir.milad.DocVisitApp.domain.visit_session.service.LoadDashboardDataService;
import ir.milad.DocVisitApp.domain.visit_session.service.LoadPatientsDataService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;

import static ir.milad.DocVisitApp.infra.web.PatientController.ERROR_500_ATTRIBUTE_NAME;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final CreateVisitSessionService createVisitSessionService;
    private final LoadDashboardDataService loadDashboardDataService;
    private final LoadPatientsDataService loadPatientsDataService;

    public DoctorController(
            CreateVisitSessionService createVisitSessionService,
            LoadDashboardDataService loadDashboardDataService,
            LoadPatientsDataService loadPatientsDataService) {
        this.createVisitSessionService = createVisitSessionService;
        this.loadDashboardDataService = loadDashboardDataService;
        this.loadPatientsDataService = loadPatientsDataService;
    }

    @PostMapping(value = "/create/visit_session")
    @ResponseBody
    public void createVisitSession(@Valid @RequestBody VisitSessionRequest request) {
        createVisitSessionService.create(
                request.date,
                request.fromTime,
                request.toTime,
                request.sessionLength
        );
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
        return Try.of(() -> loadPatientsDataService.load(1))
                .map(patientsData -> {
                    model.addAttribute("data", patientsData);
                    return "/doctor/patients :: patients";
                })
                .recover(throwable -> {
                    model.addAttribute(ERROR_500_ATTRIBUTE_NAME, "Your turn not found");
                    return "500";
                })
                .get();
    }

    private void loadDashboardInfo(Model model) {
        var dashboardInfo = loadDashboardDataService.load();
        model.addAttribute("dashboardInfo", dashboardInfo);
    }

    @NoArgsConstructor
    @Getter
    @Setter
    public static class VisitSessionRequest {
        private LocalDate date;
        private LocalTime fromTime;
        private LocalTime toTime;
        @Positive(message = "visit session length can't be lower than 0")
        private Integer sessionLength;
    }
}

package ir.milad.DocVisitApp.domain.visit_session;

import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.function.Consumer;

@Getter
@Setter
@Slf4j
public class VisitSession {
    private final String id;
    private LocalDate date;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private Integer sessionLength;
    private LocalDateTime lastAppointmentTime;
    private final List<Appointment> appointments;
    private Map<String, Long> onHoldTimes;

    public VisitSession(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        this.id = TsidCreator.getTsid().toString();
        this.date = date;
        this.fromTime = LocalDateTime.of(fromTime);
        this.toTime = LocalDateTime.of(toTime);
        this.sessionLength = sessionLength;

        if (fromTime.isAfter(toTime))
            throw new ApplicationException("Start time cant be after end time.");

        lastAppointmentTime = this.fromTime;
        appointments = new ArrayList<>();
        onHoldTimes = new HashMap<>();
    }

    public VisitSession(
            LocalDate date,
            LocalTime fromTime,
            LocalTime toTime,
            Integer sessionLength,
            LocalDateTime lastAppointmentTime,
            List<Appointment> appointments,
            Map<String, Long> onHoldTimes
    ) {
        this.id = TsidCreator.getTsid().toString();
        this.date = date;
        this.fromTime = LocalDateTime.of(fromTime);
        this.toTime = LocalDateTime.of(toTime);
        this.sessionLength = sessionLength;
        this.lastAppointmentTime = lastAppointmentTime;
        this.appointments = new ArrayList<>(appointments);
        this.onHoldTimes = onHoldTimes;
    }


    public Appointment giveAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        log.info("Event: Tying to give appointment ....");
        var patientAppointment = findActiveAppointmentByPatient(patient);
        if (patientAppointment.isPresent())
            return patientAppointment.get();

        // TODO: 8/14/2023 What if last session is lower than session length
        if (visitSessionIsOver(LocalDateTime.of(entryTime)))
            throw new ApplicationException("Session time is over. can't give new turns.");
        var appointment = giveNewAppointment(patient, entryTime, numOfPersons);
        log.info("Event: Appointment given: {}", appointment);
        return appointment;
    }

    public Patient cancelAppointment(String id, AppointmentStatus appointmentStatus, LocalTime entryTime) {
        log.info("Tying to cancel appointment with id {} .....", id);
        var errorMsg = "Cant cancel Appointment. Appointment status should be (WAITING OR ON_HOLD)";
        var appointment = loadAppointmentAndCheckItsStatus(
                id,
                Set.of(AppointmentStatus.WAITING, AppointmentStatus.ON_HOLD, AppointmentStatus.VISITING),
                errorMsg
        );
        logOperation("Before Cancellation", appointment);

        var isLastWaitingAppointment = isLastWaitingAppointmentInQueue(appointment);
        if (isLastWaitingAppointment)
            log.info("Appointment with ticket number:{} determined as last WAITING appointment", appointment.turnNumber);

        if (appointment.getStatus() == AppointmentStatus.ON_HOLD) {
            log.info("Appointment with ticket number:{} is in ON_HOLD status", appointment.turnNumber);
            appointment.setStatus(appointmentStatus);
            onHoldTimes.remove(id);
            return appointment.patient;
        }

        var remainingTime = ((long) appointment.numOfPersons * sessionLength);
        var visitToEntryTimeDiff = appointment.visitTime.timeIntervalInMinutes(entryTime);
        if (visitToEntryTimeDiff > 0)
            remainingTime -= visitToEntryTimeDiff;
        long finalRemainingTime = remainingTime;

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                appointmentStatus,
                _appointment -> {
                    if (_appointment.getStatus() == AppointmentStatus.WAITING) {
                        _appointment.decrementTurnsToAwait();
                        _appointment.decreaseVisitTime(finalRemainingTime);
                    }
                }
        );

        if (isLastWaitingAppointment)
            lastAppointmentTime = appointment.visitTime;

        logOperation("Cancellation Completed", appointment);
        return appointment.getPatient();
    }

    public void checkIn(String appointmentId, LocalTime entryTime) {
        String errorMsg = "Wrong appointment to check-in! This patient status is not WAITING.";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.WAITING, errorMsg);
        logOperation("Before CheckIn", appointment);
        appointment.setStatus(AppointmentStatus.VISITING);
        var timeToIncrease = appointment.getVisitTime().timeIntervalInMinutes(entryTime);
        appointment.setVisitTime(LocalDateTime.of(entryTime));
        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                AppointmentStatus.VISITING,
                _appointment -> _appointment.increaseVisitTime(timeToIncrease)
        );
        logOperation("CheckIn Completed", appointment);
    }

    @UnitTestRequired
    public void done(String appointmentId, LocalTime doneTime) {
        var errorMsg = "Wrong appointment to done! Doctor is not visiting this patient.";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.VISITING, errorMsg);
        logOperation("Before Done", appointment);

        var timeDiff = appointment.calculatedEndTime(sessionLength) - LocalDateTime.of(doneTime.withSecond(0));
        /*var timeDiff = Duration.between(
                appointment.calculatedEndTime(sessionLength), LocalDateTime.of(doneTime.withSecond(0))
        ).toMinutes();*/

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                AppointmentStatus.VISITED,
                _appointment -> {
                    if (_appointment.getStatus() == AppointmentStatus.WAITING)
                        _appointment.increaseVisitTime(timeDiff);
                }
        );

        if (appointments.indexOf(appointment) == appointments.size() - 1)
            lastAppointmentTime = appointment.visitTime;

        logOperation("Done Completed", appointment);
    }

    @UnitTestRequired
    public void onHold(String appointmentId, LocalTime entryTime) {
        var errorMsg = "Can't pause appointment, Patient is not in `InProgress` Status";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.VISITING, errorMsg);
        logOperation("Before OnHold", appointment);

        var remainingTime = ((long) appointment.numOfPersons * sessionLength);
        var visitToEntryTimeDiff = appointment.visitTime.toLocalTime().withSecond(0).withNano(0) -entryTime;
        if (visitToEntryTimeDiff > 0)
            remainingTime -= visitToEntryTimeDiff;
        onHoldTimes.put(appointmentId, remainingTime);

        if (appointments.indexOf(appointment) < appointments.size() - 1) {
            var nextAppointment = appointments.get(appointments.indexOf(appointment) + 1);
            nextAppointment.decreaseVisitTime(onHoldTimes.values().stream().mapToLong(value -> value).sum());
            if (nextAppointment.getVisitTime().toLocalTime() < LocalTime.nowHM())
                nextAppointment.setVisitTime(LocalDateTime.nowHM());
        }

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                AppointmentStatus.ON_HOLD,
                _appointment -> {
                    if (_appointment.status == AppointmentStatus.WAITING)
                        _appointment.increaseVisitTime(visitToEntryTimeDiff);
                }
        );

        logOperation("OnHold Completed", appointment);
    }

    @UnitTestRequired
    public void resume(String appointmentId, LocalTime entryTime) {
        var errorMsg = "Can't resume appointment, Patient is not in `On-Hold` Status";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.ON_HOLD, errorMsg);
        appointment.setStatus(AppointmentStatus.VISITING);
        logOperation("Before Resume", appointment);

        onHoldTimes.remove(appointmentId);
        appointment.visitTime = LocalDateTime.of(entryTime);
        logOperation("Resume Completed", appointment);
    }

    @UnitTestRequired
    public Optional<Appointment> findAppointmentById(String id) {
        return appointments.filter(t -> t.id.equals(id)).findFirst();
    }

    @UnitTestRequired
    public VisitSessionSummary summary() {
        var nextAppointmentId = appointments.filter(appointment -> appointment.status == AppointmentStatus.WAITING)
                .findFirst().map(Appointment::getId).orElse("-1");
        return new VisitSessionSummary(
                numberOfAppointmentsByStatus(Optional.empty()),
                numberOfAppointmentsAwaiting(),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.VISITED)),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.CANCELED)) +
                        numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.CANCELED_BY_DOCTOR)),
                nextAppointmentId,
                fromTime, toTime, sessionLength);
    }

    @UnitTestRequired
    public void update(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        if (appointments.isNotEmpty() && (!fromTime.equals(this.fromTime.toLocalTime()) || !Objects.equals(sessionLength, this.sessionLength)))
            throw new ApplicationException("Can't change session 'from time'/'session length'.");

        this.date = date;
        this.fromTime = LocalDateTime.of(fromTime);
        this.toTime = LocalDateTime.of(toTime);
        this.sessionLength = sessionLength;
    }

    @UnitTestRequired
    public long numberOfAppointmentsAwaiting() {
        return numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.WAITING));
    }

    @UnitTestRequired
    public Long numberOfAppointmentsByStatus(Optional<AppointmentStatus> status) {
        return status
                .map(appointmentStatus -> appointments.filter(appointment -> appointment.status == appointmentStatus).count())
                .orElseGet(() -> (long) appointments.size());
    }

    @UnitTestRequired
    public boolean visitSessionIsOver(LocalDateTime entryTime) {
        return entryTime > toTime || lastAppointmentTime > toTime;
    }

    @UnitTestRequired
    public Long appointmentTurnsToAwait(String appointmentId) {
        var turnsToAwait = 0L;
        for (Appointment a : appointments) {
            if (Objects.equals(a.getId(), appointmentId))
                break;
            if (a.getStatus() == AppointmentStatus.WAITING || a.getStatus() == AppointmentStatus.VISITING)
                turnsToAwait++;
        }
        return turnsToAwait;
    }

    private Appointment giveNewAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        var entryDateTime = LocalDateTime.of(entryTime);
        LocalDateTime visitTime;
        if (entryDateTime >= lastAppointmentTime) {
            visitTime = entryDateTime;
            lastAppointmentTime = entryDateTime.plusMinutes((long) sessionLength * numOfPersons);
        } else {
            visitTime = lastAppointmentTime;
            lastAppointmentTime = lastAppointmentTime.plusMinutes((long) sessionLength * numOfPersons);
        }

        var appointment = Appointment.newAppointment(appointments.size() + 1, appointments.size(), visitTime, patient, numOfPersons);
        appointments.add(appointment);
        return appointment;
    }

    private Optional<Appointment> findActiveAppointmentByPatient(Patient patient) {
        return appointments
                .filter(t -> t.isSamePatient(patient) && t.status == AppointmentStatus.WAITING)
                .findFirst();
    }

    private Appointment loadAppointmentAndCheckItsStatus(String appointmentId, AppointmentStatus status, String errorMsg) {
        var appointment = findAppointmentById(appointmentId)
                .orElseThrow(() -> new ApplicationException("Appointment with id: " + id + " Not Found"));
        if (appointment.status != status)
            throw new ApplicationException(errorMsg);
        return appointment;
    }

    private Appointment loadAppointmentAndCheckItsStatus(String appointmentId, Set<AppointmentStatus> statuses, String errorMsg) {
        var appointment = findAppointmentById(appointmentId)
                .orElseThrow(() -> new ApplicationException("Appointment with id: " + id + " Not Found"));
        if (!statuses.contains(appointment.status))
            throw new ApplicationException(errorMsg);
        return appointment;
    }

    private void updateAppointmentStatusThenRescheduleSubsequentAppointments(
            Appointment appointment,
            AppointmentStatus status,
            Consumer<Appointment> rescheduler) {
        appointment.setStatus(status);
        var index = appointments.indexOf(appointment);
        if (index == -1)
            throw new ApplicationException("Appointment with id#" + appointment.getId() + "Not Found");

        for (int i = index + 1; i < appointments.size(); i++) {
            var _appointment = appointments.get(i);
            rescheduler.accept(_appointment);
        }

        lastWaitingAppointmentInQueue().ifPresent(value ->
                lastAppointmentTime = value.getVisitTime().plusMinutes((long) value.numOfPersons * sessionLength)
        );
    }

    private boolean isLastWaitingAppointmentInQueue(Appointment appointment) {
        return lastWaitingAppointmentInQueue()
                .map(la -> la.getId().equals(appointment.getId()))
                .orElse(false);
    }

    private Optional<Appointment> lastWaitingAppointmentInQueue() {
        for (int i = appointments.size() - 1; i >= 0; i--) {
            if (appointments.get(i).getStatus() == AppointmentStatus.WAITING) {
                return Optional.of(appointments.get(i));
            }
        }
        return Optional.empty();
    }

    private void logOperation(String operation, Appointment appointment) {
        var message = new StringBuilder()
                .append("Event: ")
                .append(operation)
                .append(", Ticket Number:")
                .append(appointment.turnNumber)
                .append(", Visit Time:")
                .append(appointment.visitTime.toLocalTime())
                .append(", lastAppointmentTime: ")
                .append(lastAppointmentTime.toLocalTime())
                .append(", onHoldTimes:")
                .append(onHoldTimes);
        log.info(message.toString());
    }

}

package ir.milad.DocVisitApp.domain.visit_session;

import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.UnitTestRequired;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;

@Getter
@Setter
public class VisitSession {
    private final String id;
    private LocalDate date;
    private LocalDateTime fromTime;
    private LocalDateTime toTime;
    private Integer sessionLength;
    private LocalDateTime lastAppointmentTime;
    private final LinkedList<Appointment> appointments;

    public VisitSession(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        this.id = TsidCreator.getTsid().toString();
        this.date = date;
        this.fromTime = LocalDateTime.of(LocalDate.now(), fromTime);
        this.toTime = LocalDateTime.of(LocalDate.now(), toTime);
        this.sessionLength = sessionLength;

        if (fromTime.isAfter(toTime))
            throw new ApplicationException("Start time cant be after end time.");

        lastAppointmentTime = this.fromTime;
        appointments = new LinkedList<>();
    }

    public Appointment giveAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        var patientAppointment = findActiveAppointmentByPatient(patient);
        if (patientAppointment.isPresent())
            return patientAppointment.get();

        // TODO: 8/14/2023 What if last session is lower than session length
        if (visitSessionIsOver(LocalDateTime.of(LocalDate.now(), entryTime)))
            throw new ApplicationException("Session time is over. can't give new turns.");

        return giveNewAppointment(patient, entryTime, numOfPersons);
    }

    public Patient cancelAppointment(String id, AppointmentStatus appointmentStatus, LocalTime entryTime) {
        var errorMsg = "Cant cancel Appointment. Appointment status should be (WAITING OR ON_HOLD)";
        var appointment = loadAppointmentAndCheckItsStatus(
                id,
                Set.of(AppointmentStatus.WAITING, AppointmentStatus.ON_HOLD, AppointmentStatus.VISITING),
                errorMsg
        );

        if (appointment.getStatus() == AppointmentStatus.ON_HOLD) {
            appointment.setStatus(appointmentStatus);
            return appointment.patient;
        }

        var remainingTime = ((long) appointment.numOfPersons * sessionLength);
        var visitToEntryTimeDiff = Duration.between(appointment.visitTime.toLocalTime(), entryTime).toMinutes();
        if (visitToEntryTimeDiff > 0)
            remainingTime -= Duration.between(appointment.visitTime.toLocalTime(), entryTime).toMinutes();
        long finalRemainingTime = remainingTime;

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                appointmentStatus,
                (index, _appointment) -> {
                    if (_appointment.getStatus() == AppointmentStatus.WAITING) {
                        _appointment.decrementTurnsToAwait();
                        _appointment.decreaseVisitTime(finalRemainingTime);
                    }
                }
        );

        if (appointments.indexOf(appointment) == appointments.indexOf(appointments.getLast()))
            lastAppointmentTime = appointment.visitTime;

        return appointment.getPatient();
    }

    @UnitTestRequired
    public void checkIn(String appointmentId, LocalTime entryTime) {
        String errorMsg = "Wrong appointment to check-in! This patient status is not WAITING.";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.WAITING, errorMsg);
        if (Objects.equals(appointment.getVisitTime().toLocalTime(), entryTime)) {
            appointment.setStatus(AppointmentStatus.VISITING);
        } else if (appointment.getVisitTime().toLocalTime().isBefore(entryTime)) {
            var timeToIncrease = Duration.between(appointment.getVisitTime(), LocalDateTime.now()).toMinutes();
            appointment.setVisitTime(LocalDateTime.of(LocalDate.now(), entryTime));
            updateAppointmentStatusThenRescheduleSubsequentAppointments(
                    appointment,
                    AppointmentStatus.VISITING,
                    (integer, _appointment) -> {
                        _appointment.increaseVisitTime(timeToIncrease);
                    }
            );
        }
    }

    @UnitTestRequired
    public void done(String appointmentId, LocalTime doneTime) {
        var errorMsg = "Wrong appointment to done! Doctor is not visiting this patient.";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.VISITING, errorMsg);
        var timeDiff = Duration.between(
                appointment.calculatedEndTime(sessionLength), LocalDateTime.of(LocalDate.now(), doneTime.withSecond(0))
        ).toMinutes();

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                AppointmentStatus.VISITED,
                (index, _appointment) -> {
                    if (_appointment.getStatus() == AppointmentStatus.WAITING)
                        _appointment.increaseVisitTime(timeDiff);
                }
        );

        var index = appointments.indexOf(appointment);
        for (int i = index + 1; i < appointments.size(); i++) {
            var _appointment = appointments.get(i);
            if (_appointment.getTurnNumber() == 15) {
                _appointment.setVisitTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 29)));
            }
            if (_appointment.getTurnNumber() == 16) {
                _appointment.setVisitTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 37)));
            }
            if (_appointment.getTurnNumber() == 17) {
                _appointment.setVisitTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 45)));
            }
            if (_appointment.getTurnNumber() == 18) {
                _appointment.setVisitTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(19, 53)));
            }
            if (_appointment.getTurnNumber() == 20) {
                _appointment.setVisitTime(LocalDateTime.of(LocalDate.now(), LocalTime.of(20, 01)));
            }
        }

        if (appointments.indexOf(appointment) == appointments.indexOf(appointments.getLast()))
            lastAppointmentTime = appointment.visitTime;
    }

    @UnitTestRequired
    public void onHold(String appointmentId) {
        var errorMsg = "Can't pause appointment, Patient is not in `InProgress` Status";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.VISITING, errorMsg);

        var remainingTime = ((long) appointment.numOfPersons * sessionLength);
        var visitToEntryTimeDiff = Duration.between(appointment.visitTime.toLocalTime(), LocalDateTime.now()).toMinutes();
        if (visitToEntryTimeDiff > 0)
            remainingTime -= Duration.between(appointment.visitTime.toLocalTime(), LocalDateTime.now()).toMinutes();
        long finalRemainingTime = remainingTime;

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                AppointmentStatus.ON_HOLD,
                (index, _appointment) -> {
                    if (_appointment.status == AppointmentStatus.WAITING) {
                        _appointment.decrementTurnsToAwait();
                        _appointment.decreaseVisitTime(finalRemainingTime);
                    }
                }
        );
    }

    @UnitTestRequired
    public void resume(String appointmentId) {
        var errorMsg = "Can't resume appointment, Patient is not in `On-Hold` Status";
        var appointment = loadAppointmentAndCheckItsStatus(appointmentId, AppointmentStatus.ON_HOLD, errorMsg);

        var remainingTime = ((long) appointment.numOfPersons * sessionLength);

        updateAppointmentStatusThenRescheduleSubsequentAppointments(
                appointment,
                AppointmentStatus.VISITING,
                (index, _appointment) -> {
                    if (_appointment.getStatus() == AppointmentStatus.WAITING) {
                        _appointment.incrementTurnsToAwait();
                        _appointment.increaseVisitTime(remainingTime);
                    }
                }
        );
        appointment.visitTime = LocalDateTime.of(LocalDate.now(), LocalTime.now().withSecond(0).withNano(0));
    }

    @UnitTestRequired
    public Optional<Appointment> findAppointmentById(String id) {
        return appointments.stream().filter(t -> t.id.equals(id)).findFirst();
    }

    @UnitTestRequired
    public VisitSessionSummary summary() {
        var nextAppointmentId = appointments.stream().filter(appointment -> appointment.status == AppointmentStatus.WAITING)
                .findFirst().map(Appointment::getId).orElse("-1");
        return new VisitSessionSummary(
                numberOfAppointmentsByStatus(Optional.empty()),
                numberOfAppointmentsAwaiting(),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.VISITED)),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.CANCELED)),
                nextAppointmentId);
    }

    @UnitTestRequired
    public void update(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        if (appointments.size() > 0 && ( !fromTime.equals(this.fromTime.toLocalTime()) || !Objects.equals(sessionLength, this.sessionLength)))
            throw new ApplicationException("Can't change session 'from time'/'session length'. Reason: Patients are waiting.");

        this.date = date;
        this.fromTime = LocalDateTime.of(LocalDate.now(), fromTime);
        this.toTime = LocalDateTime.of(LocalDate.now(), toTime);
        this.sessionLength = sessionLength;
    }

    @UnitTestRequired
    public long numberOfAppointmentsAwaiting() {
        return numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.WAITING));
    }

    @UnitTestRequired
    public Long numberOfAppointmentsByStatus(Optional<AppointmentStatus> status) {
        return status
                .map(appointmentStatus -> appointments.stream().filter(appointment -> appointment.status == appointmentStatus).count())
                .orElseGet(() -> (long) appointments.size());
    }

    @UnitTestRequired
    public boolean visitSessionIsOver(LocalDateTime entryTime) {
        return entryTime.isAfter(toTime) || lastAppointmentTime.isAfter(toTime);
    }

    @UnitTestRequired
    public Long appointmentTurnsToAwait(String appointmentId) {
        var turnsToAwait = 0L;
        for (int i = 0; i < appointments.size(); i++) {
            var a = appointments.get(i);
            if (Objects.equals(a.getId(), appointmentId))
                break;
            if (a.getStatus() == AppointmentStatus.WAITING || a.getStatus() == AppointmentStatus.VISITING)
                turnsToAwait++;
        }
        return turnsToAwait;
    }

    private Appointment giveNewAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        var entryDateTime = LocalDateTime.of(LocalDate.now(), entryTime);
        LocalDateTime visitTime;
        if (entryDateTime.isAfter(lastAppointmentTime) || entryDateTime.isEqual(lastAppointmentTime)) {
            visitTime = entryDateTime;
            lastAppointmentTime = entryDateTime.plusMinutes((long) sessionLength * numOfPersons);
        } else {
            visitTime = lastAppointmentTime;
            lastAppointmentTime = lastAppointmentTime.plusMinutes((long) sessionLength * numOfPersons);
        }

        var appointment = new Appointment(appointments.size() + 1, appointments.size(), visitTime, patient, numOfPersons);
        appointments.offer(appointment);
        return appointment;
    }

    private Optional<Appointment> findActiveAppointmentByPatient(Patient patient) {
        return appointments
                .stream()
                .filter(t -> t.isSamePatient(patient) && t.status == AppointmentStatus.WAITING)
                .findFirst();
    }

    private Appointment loadAppointmentAndCheckItsStatus(
            String appointmentId,
            AppointmentStatus status,
            String errorMsg) {
        var appointment = findAppointmentById(appointmentId)
                .orElseThrow(() -> new ApplicationException("Appointment with id: " + id + " Not Found"));
        if (appointment.status != status)
            throw new ApplicationException(errorMsg);
        return appointment;
    }

    private Appointment loadAppointmentAndCheckItsStatus(
            String appointmentId,
            Set<AppointmentStatus> statuses,
            String errorMsg) {
        var appointment = findAppointmentById(appointmentId)
                .orElseThrow(() -> new ApplicationException("Appointment with id: " + id + " Not Found"));
        if (!statuses.contains(appointment.status))
            throw new ApplicationException(errorMsg);
        return appointment;
    }

    private void updateAppointmentStatusThenRescheduleSubsequentAppointments(
            Appointment appointment,
            AppointmentStatus status,
            BiConsumer<Integer, Appointment> rescheduler) {
        appointment.setStatus(status);
        var index = appointments.indexOf(appointment);
        if (index == -1)
            throw new ApplicationException("Appointment with id#" + appointment.getId() + "Not Found");

        for (int i = index + 1; i < appointments.size(); i++) {
            var _appointment = appointments.get(i);
            rescheduler.accept(i, _appointment);
        }

        var lastAppointment = appointments.getLast();
        lastAppointmentTime = lastAppointment.getVisitTime().plusMinutes((long) lastAppointment.numOfPersons * sessionLength);
    }

}

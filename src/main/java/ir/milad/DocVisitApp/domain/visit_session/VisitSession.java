package ir.milad.DocVisitApp.domain.visit_session;

import ir.milad.DocVisitApp.domain.ApplicationException;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Optional;
import java.util.UUID;

@Getter
@Setter
public class VisitSession {

    private final UUID id;
    private final LocalDate date;
    private final LocalTime fromTime;
    private final LocalTime toTime;
    private final Integer sessionLength;
    private LocalTime lastTurnEndTime;
    private final LinkedList<Appointment> appointments;

    public VisitSession(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        this.id = UUID.randomUUID();
        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.sessionLength = sessionLength;

        if (fromTime.isAfter(toTime))
            throw new ApplicationException("Start time cant be after end time.");

        lastTurnEndTime = fromTime;
        appointments = new LinkedList<>();
    }

    public Appointment giveAppointment(Patient patient, LocalTime entryTime) {
        // TODO: 8/14/2023 What if last session is lower than session length
        if (visitSessionIsOver(entryTime))
            throw new ApplicationException("Session time is over. can't give new turns.");

        return findActiveAppointmentByPatient(patient)
                .orElseGet(() -> giveNewAppointment(patient, entryTime));
    }

    public void cancelAppointment(String id) {
        // TODO: 8/25/2023 Use manifold String interpolation
        var turn = findTurnById(id).orElseThrow(() -> new ApplicationException("Turn with id: " + id + " Not Found"));
        if (turn.status != AppointmentStatus.WAITING)
            throw new ApplicationException("Cant cancel turn");

        turn.status = AppointmentStatus.CANCELED;
        var index = appointments.indexOf(turn);
        for (int i = appointments.size() - 1; i > index; i--) {
            var reference = appointments.get(i - 1);
            var toChange = appointments.get(i);
            toChange.turnNumber = reference.turnNumber;
            toChange.turnsToAwait = reference.turnsToAwait;
            toChange.visitTime = reference.visitTime;
        }

        lastTurnEndTime = appointments.getLast().visitTime.plusMinutes(sessionLength);
    }

    public Optional<Appointment> findTurnById(String id) {
        return appointments.stream().filter(t -> t.id.equals(id)).findFirst();
    }

    public VisitSessionSummary summary() {
        return new VisitSessionSummary(
                numberOfAppointmentsByStatus(Optional.empty()),
                numberOfAppointmentsAwaiting(),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.VISITED)),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.CANCELED)),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.EXPIRED)));
    }

    public long numberOfAppointmentsAwaiting() {
        return numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.WAITING));
    }

    private Long numberOfAppointmentsByStatus(Optional<AppointmentStatus> status) {
        return status
                .map(appointmentStatus -> appointments.stream().filter(appointment -> appointment.status == appointmentStatus).count())
                .orElseGet(() -> (long) appointments.size());
    }

    private Appointment giveNewAppointment(Patient patient, LocalTime entryTime) {
        LocalTime visitTime;
        if (entryTime.isAfter(lastTurnEndTime)) {
            visitTime = entryTime;
            lastTurnEndTime = entryTime.plusMinutes(sessionLength);
        } else {
            visitTime = lastTurnEndTime;
            lastTurnEndTime = lastTurnEndTime.plusMinutes(sessionLength);
        }

        var appointment = new Appointment(appointments.size() + 1, appointments.size(), visitTime, patient);
        appointments.offer(appointment);
        return appointment;
    }

    private Optional<Appointment> findActiveAppointmentByPatient(Patient patient) {
        return appointments
                .stream()
                .filter(t -> t.isSamePatient(patient) && t.status == AppointmentStatus.WAITING)
                .findFirst();
    }

    private boolean visitSessionIsOver(LocalTime entryTime) {
        return entryTime.isAfter(toTime) || lastTurnEndTime.isAfter(toTime);
    }

}

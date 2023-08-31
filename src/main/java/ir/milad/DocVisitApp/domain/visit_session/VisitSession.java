package ir.milad.DocVisitApp.domain.visit_session;

import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Objects;
import java.util.Optional;

@Getter
@Setter
public class VisitSession {

    private final static int APPOINTMENT_BEGIN_NUMBER = 99;

    private final String id;
    private LocalDate date;
    private LocalTime fromTime;
    private LocalTime toTime;
    private Integer sessionLength;
    private LocalTime lastAppointmentTime;
    private final LinkedList<Appointment> appointments;

    public VisitSession(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        this.id = TsidCreator.getTsid().toString();
        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.sessionLength = sessionLength;

        if (fromTime.isAfter(toTime))
            throw new ApplicationException("Start time cant be after end time.");

        lastAppointmentTime = fromTime;
        appointments = new LinkedList<>();
    }

    public Appointment giveAppointment(Patient patient, LocalTime entryTime) {
        // TODO: 8/14/2023 What if last session is lower than session length
        if (visitSessionIsOver(entryTime))
            throw new ApplicationException("Session time is over. can't give new turns.");

        return findActiveAppointmentByPatient(patient)
                .orElseGet(() -> giveNewAppointment(patient, entryTime));
    }

    public Patient cancelAppointment(String id) {
        // TODO: 8/25/2023 Use manifold String interpolation
        var appointment = findAppointmentById(id)
                .orElseThrow(() -> new ApplicationException("Turn with id: " + id + " Not Found"));
        if (appointment.status != AppointmentStatus.WAITING)
            throw new ApplicationException("Cant cancel turn");

        appointment.status = AppointmentStatus.CANCELED;
        var index = appointments.indexOf(appointment);
        for (int i = appointments.size() - 1; i > index; i--) {
            var reference = appointments.get(i - 1);
            var toChange = appointments.get(i);
            toChange.turnNumber = reference.turnNumber;
            toChange.turnsToAwait = reference.turnsToAwait;
            toChange.visitTime = reference.visitTime;
        }

        lastAppointmentTime = appointments.getLast().visitTime.plusMinutes(sessionLength);
        return appointment.getPatient();
    }

    public void checkIn(String appointmentId) {
        var appointment = findAppointmentById(appointmentId)
                .orElseThrow(() -> new ApplicationException("Appointment didnt found:" + id));
        if (appointment.status != AppointmentStatus.WAITING)
            throw new ApplicationException("Wrong appointment to check-in! This patient status is not WAITING.");
        // TODO: 8/29/2023 Check first waiting person can check in?
        appointment.setStatus(AppointmentStatus.VISITING);
    }

    public void done(String appointmentId, LocalTime doneTime) {
        var appointment = findAppointmentById(appointmentId)
                .orElseThrow(() -> new ApplicationException("Appointment didnt found:" + id));
        if (appointment.status != AppointmentStatus.VISITING)
            throw new ApplicationException("Wrong appointment to done! Doctor is not visiting this patient.");
        appointment.setStatus(AppointmentStatus.VISITED);
        var index = appointments.indexOf(appointment);
        var count = 0;
        var refTime = doneTime.withSecond(0);
        for (int i = index + 1; i < appointments.size(); i++) {
            appointments.get(i).visitTime = refTime.plusMinutes(count * sessionLength);
            count++;
        }
        lastAppointmentTime = refTime.plusMinutes(count);
    }

    public Optional<Appointment> findAppointmentById(String id) {
        return appointments.stream().filter(t -> t.id.equals(id)).findFirst();
    }

    public VisitSessionSummary summary() {
        var nextAppointmentId = appointments.stream().filter(appointment -> appointment.status == AppointmentStatus.WAITING)
                .findFirst().map(Appointment::getId).orElse("-1");
        return new VisitSessionSummary(
                numberOfAppointmentsByStatus(Optional.empty()),
                numberOfAppointmentsAwaiting(),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.VISITED)),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.CANCELED)),
                numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.EXPIRED)),
                nextAppointmentId);
    }

    public void update(LocalDate date, LocalTime fromTime, LocalTime toTime, Integer sessionLength) {
        if (appointments.size() > 0 && ( !fromTime.equals(this.fromTime) || Objects.equals(sessionLength, this.sessionLength)))
            throw new ApplicationException("Can't change session from time/session length. Reason: Patients are in waiting.");

        this.date = date;
        this.fromTime = fromTime;
        this.toTime = toTime;
        this.sessionLength = sessionLength;
    }

    public long numberOfAppointmentsAwaiting() {
        return numberOfAppointmentsByStatus(Optional.of(AppointmentStatus.WAITING));
    }

    public Long numberOfAppointmentsByStatus(Optional<AppointmentStatus> status) {
        return status
                .map(appointmentStatus -> appointments.stream().filter(appointment -> appointment.status == appointmentStatus).count())
                .orElseGet(() -> (long) appointments.size());
    }

    private Appointment giveNewAppointment(Patient patient, LocalTime entryTime) {
        LocalTime visitTime;
        if (entryTime.isAfter(lastAppointmentTime)) {
            visitTime = entryTime;
            lastAppointmentTime = entryTime.plusMinutes(sessionLength);
        } else {
            visitTime = lastAppointmentTime;
            lastAppointmentTime = lastAppointmentTime.plusMinutes(sessionLength);
        }

        var appointment = new Appointment(appointments.size() + 1 + APPOINTMENT_BEGIN_NUMBER, appointments.size(), visitTime, patient);
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
        return entryTime.isAfter(toTime) || lastAppointmentTime.isAfter(toTime);
    }

}

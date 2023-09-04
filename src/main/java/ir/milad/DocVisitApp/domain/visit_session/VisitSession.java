package ir.milad.DocVisitApp.domain.visit_session;

import com.github.f4b6a3.tsid.TsidCreator;
import ir.milad.DocVisitApp.domain.ApplicationException;
import ir.milad.DocVisitApp.domain.patient.Patient;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.Optional;

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

    public Patient cancelAppointment(String id) {
        // TODO: 8/25/2023 Use manifold String interpolation
        var appointment = findAppointmentById(id)
                .orElseThrow(() -> new ApplicationException("Turn with id: " + id + " Not Found"));
        if (appointment.status != AppointmentStatus.WAITING)
            throw new ApplicationException("Cant cancel turn");

        appointment.status = AppointmentStatus.CANCELED;
        var index = appointments.indexOf(appointment);
        for (int i = index + 1; i < appointments.size(); i++) {
            var _appointment = appointments.get(i);
            _appointment.setTurnNumber(_appointment.turnNumber - 1);
            _appointment.setTurnsToAwait(_appointment.turnsToAwait - 1);
            _appointment.setVisitTime(_appointment.visitTime.minusMinutes((long) appointment.numOfPersons * sessionLength));
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
        var refTime = LocalDateTime.of(LocalDate.now(), doneTime.withSecond(0));
        for (int i = index + 1; i < appointments.size(); i++) {
            var _appointment = appointments.get(i);
            _appointment.visitTime = refTime.plusMinutes((long) count * sessionLength);
            count += _appointment.numOfPersons;
        }
        lastAppointmentTime = refTime.plusMinutes((long) count * sessionLength);
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
        /*if (appointments.size() > 0 && ( !fromTime.equals(this.fromTime) || !Objects.equals(sessionLength, this.sessionLength)))
            throw new ApplicationException("Can't change session from time/session length. Reason: Patients are in waiting.");*/

        this.date = date;
        this.fromTime = LocalDateTime.of(LocalDate.now(), fromTime);
        this.toTime = LocalDateTime.of(LocalDate.now(), toTime);
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

    private Appointment giveNewAppointment(Patient patient, LocalTime entryTime, int numOfPersons) {
        var entryDateTime = LocalDateTime.of(LocalDate.now(), entryTime);
        LocalDateTime visitTime;
        if (entryDateTime.isAfter(lastAppointmentTime)) {
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

    public boolean visitSessionIsOver(LocalDateTime entryTime) {
        return entryTime.isAfter(toTime) || lastAppointmentTime.isAfter(toTime);
    }

}

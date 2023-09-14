package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.Appointment;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.AppointmentEntity;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.PatientEntity;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.VisitSessionEntity;
import one.microstream.storage.types.StorageManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

@Repository
public class MicroStreamVisitSessionRepository implements VisitSessionRepository {

    private final ReadWriteLock lock;
    private final DatabaseV2 database;

    public MicroStreamVisitSessionRepository(@Qualifier("appStorageManager") StorageManager storageManager) {
        this.database = (DatabaseV2) storageManager.root();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public void setActiveVisitSession(VisitSession visitSession) {
        writeWithLock(() -> database.setActiveVisitSession(VisitSessionEntity.from(visitSession)));
    }

    @Override
    public Optional<VisitSession> getActiveSession(LocalDateTime dateTime) {
        return readWithLock(() -> database.getActiveSession(dateTime))
                .map(this::from);
    }

    @Override
    public Optional<VisitSession> getActiveSession(LocalDate date) {
        return readWithLock(() -> database.getActiveSession(date))
                .map(this::from);
    }

    @Override
    public void clearActiveVisitSession() {
        writeWithLock(database::clearActiveVisitSession);
    }

    @Override
    public void updateActiveVisitSession(VisitSession visitSession) {
        writeWithLock(() -> database.updateActiveVisitSession(VisitSessionEntity.from(visitSession)));
    }

    @Override
    public List<VisitSession> getVisitSessionHistories() {
        return readWithLock(() -> database.visitSessionsHistory.get()).map(this::from).toList();
    }

    @Override
    public boolean hasHistory(Patient patient) {
        return readWithLock(() -> database.existsHistoryFor(PatientEntity.from(patient)));
    }

    private <O> O readWithLock(Supplier<O> dbOperation) {
        lock.readLock().lock();
        try {
            return dbOperation.get();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void writeWithLock(Runnable dbOperation) {
        lock.writeLock().lock();
        try {
            dbOperation.run();
        } finally {
            lock.writeLock().unlock();
        }
    }

    private VisitSession from(VisitSessionEntity visitSessionEntity) {
        return new VisitSession(
                visitSessionEntity.getDate(),
                visitSessionEntity.getFromTime().toLocalTime(),
                visitSessionEntity.getToTime().toLocalTime(),
                visitSessionEntity.getSessionLength(),
                visitSessionEntity.getLastAppointmentTime(),
                visitSessionEntity.getAppointments().map(this::from).toList(),
                visitSessionEntity.getOnHoldTimes()
        );
    }

    private Appointment from(AppointmentEntity appointmentEntity) {
        var patientEntity = new Patient(
                appointmentEntity.getPatient().getPhoneNumber(),
                appointmentEntity.getPatient().getFirstName(),
                appointmentEntity.getPatient().getLastName(),
                appointmentEntity.getPatient().getDateOfBirth()
        );
        return new Appointment(
                appointmentEntity.getId(),
                appointmentEntity.getTurnNumber(),
                appointmentEntity.getTurnsToAwait(),
                appointmentEntity.getVisitTime(),
                patientEntity,
                appointmentEntity.getStatus(),
                appointmentEntity.getNumOfPersons()
        );
    }
}

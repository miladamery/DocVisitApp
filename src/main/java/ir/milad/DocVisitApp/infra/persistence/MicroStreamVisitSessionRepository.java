package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import one.microstream.storage.types.StorageManager;
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
    private final Database database;

    public MicroStreamVisitSessionRepository(StorageManager storageManager) {
        this.database = (Database) storageManager.root();
        lock = new ReentrantReadWriteLock();
    }

    @Override
    public void setActiveVisitSession(VisitSession visitSession) {
        writeWithLock(() -> database.setActiveVisitSession(visitSession));
    }

    @Override
    public Optional<VisitSession> getActiveSession(LocalDateTime dateTime) {
        return readWithLock(() -> database.getActiveSession(dateTime));
    }

    @Override
    public Optional<VisitSession> getActiveSession(LocalDate date) {
        return readWithLock(() -> database.getActiveSession(date));
    }

    @Override
    public void clearActiveVisitSession() {
        writeWithLock(database::clearActiveVisitSession);
    }

    @Override
    public void updateActiveVisitSession() {
        writeWithLock(database::updateActiveVisitSession);
    }

    @Override
    public List<VisitSession> getVisitSessionHistories() {
        return readWithLock(() -> database.visitSessionsHistory.get());
    }

    @Override
    public boolean hasHistory(Patient patient) {
        return readWithLock(() -> database.existsHistoryFor(patient));
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
}

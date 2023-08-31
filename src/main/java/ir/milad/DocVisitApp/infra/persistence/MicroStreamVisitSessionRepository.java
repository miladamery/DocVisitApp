package ir.milad.DocVisitApp.infra.persistence;

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
        lock.writeLock().lock();
        try {
            database.setActiveVisitSession(visitSession);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<VisitSession> getActiveSession(LocalDateTime dateTime) {
        lock.readLock().lock();
        try {
            return database.getActiveSession(dateTime);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public Optional<VisitSession> getActiveSession(LocalDate date) {
        return database.getActiveSession(date);
    }

    @Override
    public boolean exists(LocalDate date) {
        lock.readLock().lock();
        try {
            return database.hasActiveVisitSession(date);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void updateActiveVisitSession() {
        lock.writeLock().lock();
        try {
            database.updateActiveVisitSession();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<VisitSession> getVisitSessionHistories() {
        lock.readLock().lock();
        try {
            return database.visitSessionsHistory.get();
        } finally {
            lock.readLock().unlock();
        }
    }
}

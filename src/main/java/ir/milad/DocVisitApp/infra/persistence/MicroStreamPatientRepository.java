package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import one.microstream.storage.types.StorageManager;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Repository
public class MicroStreamPatientRepository implements PatientRepository {
    private final ReadWriteLock lock;
    private final Database database;

    public MicroStreamPatientRepository(StorageManager storageManager) {
        this.lock = new ReentrantReadWriteLock();
        this.database = (Database) storageManager.root();
    }

    @Override
    public void addPatientHistory(Patient patient, PatientHistory history) {
        lock.writeLock().lock();
        try {
            database.addPatientHistory(patient, history);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<PatientHistory> findHistoryByPatient(Patient patient) {
        lock.readLock().lock();
        try {
            return database.findHistoryByPatient(patient);
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addToBlocked(Patient patient) {
        lock.writeLock().lock();
        try {
            database.addToBlocked(patient);
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isBlocked(Patient patient) {
        lock.readLock().lock();
        try {
            return database.blockedPatientsRef.get().contains(patient);
        } finally {
            lock.readLock().unlock();
        }
    }
}

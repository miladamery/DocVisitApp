package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.patient.PatientRepository;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.PatientEntity;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.PatientHistoryEntity;
import one.microstream.storage.types.StorageManager;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Repository
public class MicroStreamPatientRepository implements PatientRepository {
    private final ReadWriteLock lock;
    private final DatabaseV2 database;

    public MicroStreamPatientRepository(@Qualifier("appStorageManager") StorageManager storageManager) {
        this.lock = new ReentrantReadWriteLock();
        this.database = (DatabaseV2) storageManager.root();
    }

    @Override
    public void addPatientHistory(Patient patient, PatientHistory history) {
        lock.writeLock().lock();
        try {
            database.addPatientHistory(PatientEntity.from(patient), PatientHistoryEntity.from(history));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public List<PatientHistory> findHistoryByPatient(Patient patient) {
        lock.readLock().lock();
        try {
            return database.findHistoryByPatient(PatientEntity.from(patient))
                    .map(phe -> new PatientHistory(phe.date, phe.status))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public void addToBlocked(Patient patient) {
        lock.writeLock().lock();
        try {
            database.addToBlocked(PatientEntity.from(patient));
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public boolean isBlocked(Patient patient) {
        lock.readLock().lock();
        try {
            return database.blockedPatientsRef.get().contains(PatientEntity.from(patient));
        } finally {
            lock.readLock().unlock();
        }
    }
}

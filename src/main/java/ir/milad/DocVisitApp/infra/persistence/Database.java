package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistory;
import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import jakarta.annotation.PreDestroy;
import one.microstream.integrations.spring.boot.types.Storage;
import one.microstream.reference.Lazy;
import one.microstream.storage.types.StorageManager;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Storage
public class Database {

    // Constructor injection not supported for @Storage
    @Autowired
    private transient StorageManager storageManager;

    Optional<VisitSession> activeVisitSession = Optional.empty();
    Lazy<List<VisitSession>> visitSessionsHistory = Lazy.Reference(new ArrayList<>());
    Lazy<Map<Patient, List<PatientHistory>>> patientsHistoryRef = Lazy.Reference(new HashMap<>());
    Lazy<Set<Patient>> blockedPatientsRef = Lazy.Reference(new HashSet<>());

    public void setActiveVisitSession(VisitSession visitSession) {
        Objects.requireNonNull(visitSession, "Database.setCurrentActiveVisitSession can't accept null visitSession");
        visitSessionsHistory.get().add(visitSession);
        activeVisitSession = Optional.of(visitSession);
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.storeAll(this);
        eagerStorer.commit();
    }

    public void updateActiveVisitSession() {
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.store(activeVisitSession);
        eagerStorer.commit();
    }

    public Optional<VisitSession> getActiveSession(LocalDateTime dateTime) {
        return getActiveSession(dateTime.toLocalDate())
                .filter(vs ->
                        dateTime.toLocalTime().equals(vs.getFromTime()) || dateTime.toLocalTime().isAfter(vs.getFromTime().toLocalTime()) &&
                                dateTime.toLocalTime().equals(vs.getToTime()) || dateTime.toLocalTime().isBefore(vs.getToTime().toLocalTime())
                );
    }

    public Optional<VisitSession> getActiveSession(LocalDate date) {
        return activeVisitSession.filter(vs -> vs.getDate().equals(date));
    }

    public void addPatientHistory(Patient patient, PatientHistory history) {
        var histories = patientsHistoryRef.get();
        var patientHistories = histories.getOrDefault(patient, new ArrayList<>());
        patientHistories.add(history);
        histories.put(patient, patientHistories);
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.store(patientsHistoryRef);
        eagerStorer.commit();
    }

    public List<PatientHistory> findHistoryByPatient(Patient patient) {
        return patientsHistoryRef.get().getOrDefault(patient, new ArrayList<>());
    }

    public Boolean existsHistoryFor(Patient patient) {
        return patientsHistoryRef.get().containsKey(patient);
    }

    public void addToBlocked(Patient patient) {
        var blockedPatients = blockedPatientsRef.get();
        blockedPatients.add(patient);
        storageManager.store(blockedPatients);
    }

    public void clearActiveVisitSession() {
        activeVisitSession = Optional.empty();
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.store(this);
        eagerStorer.commit();
    }

    @PreDestroy
    public void onDestroy() {
        storageManager.shutdown();
    }
}

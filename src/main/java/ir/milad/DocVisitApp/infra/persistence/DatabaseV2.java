package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.infra.persistence.entity.v2.PatientEntity;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.PatientHistoryEntity;
import ir.milad.DocVisitApp.infra.persistence.entity.v2.VisitSessionEntity;
import lombok.NoArgsConstructor;
import one.microstream.reference.Lazy;
import one.microstream.storage.types.StorageManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@NoArgsConstructor
public class DatabaseV2 {

    private transient StorageManager storageManager;

    Optional<VisitSessionEntity> activeVisitSession = Optional.empty();
    Lazy<List<VisitSessionEntity>> visitSessionsHistory = Lazy.Reference(new ArrayList<>());
    Lazy<Map<PatientEntity, List<PatientHistoryEntity>>> patientsHistoryRef = Lazy.Reference(new HashMap<>());
    Lazy<Set<PatientEntity>> blockedPatientsRef = Lazy.Reference(new HashSet<>());

    public static DatabaseV2 from(Database database) {
        Map<PatientEntity, List<PatientHistoryEntity>> patientHistory =
                database.patientsHistoryRef.get().entrySet().toMap(
                        e -> PatientEntity.from(e.getKey()),
                        e -> e.getValue().map(PatientHistoryEntity::from).collect(Collectors.toList())
                );
        return new DatabaseV2(
                database.activeVisitSession.map(VisitSessionEntity::from),
                Lazy.Reference(database.visitSessionsHistory.get().map(VisitSessionEntity::from).collect(Collectors.toList())),
                Lazy.Reference(patientHistory),
                Lazy.Reference(database.blockedPatientsRef.get().map(PatientEntity::from).collect(Collectors.toSet()))
        );
    }

    public DatabaseV2(Optional<VisitSessionEntity> activeVisitSession,
                      Lazy<List<VisitSessionEntity>> visitSessionsHistory,
                      Lazy<Map<PatientEntity, List<PatientHistoryEntity>>> patientsHistoryRef,
                      Lazy<Set<PatientEntity>> blockedPatientsRef
    ) {
        this.activeVisitSession = activeVisitSession;
        this.visitSessionsHistory = visitSessionsHistory;
        this.patientsHistoryRef = patientsHistoryRef;
        this.blockedPatientsRef = blockedPatientsRef;
    }

    public void setActiveVisitSession(VisitSessionEntity visitSessionEntity) {
        Objects.requireNonNull(visitSessionEntity, "Database.setCurrentActiveVisitSession can't accept null visitSession");
        activeVisitSession.ifPresent(vs -> visitSessionsHistory.get().add(vs));
        activeVisitSession = Optional.of(visitSessionEntity);
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.storeAll(this);
        eagerStorer.commit();
    }

    public void updateActiveVisitSession(VisitSessionEntity visitSessionEntity) {
        var eagerStorer = storageManager.createEagerStorer();
        activeVisitSession = Optional.of(visitSessionEntity);
        eagerStorer.store(this);
        eagerStorer.commit();
    }

    public Optional<VisitSessionEntity> getActiveSession(LocalDateTime dateTime) {
        return getActiveSession(dateTime.toLocalDate())
                .filter(vs -> dateTime >= vs.getFromTime() || dateTime <= vs.getToTime());
    }

    public Optional<VisitSessionEntity> getActiveSession(LocalDate date) {
        return activeVisitSession.filter(vs -> vs.getDate().equals(date));
    }

    public void addPatientHistory(PatientEntity patient, PatientHistoryEntity history) {
        var histories = patientsHistoryRef.get();
        var patientHistories = histories.getOrDefault(patient, new ArrayList<>());
        patientHistories.add(history);
        histories.put(patient, patientHistories);
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.store(patientsHistoryRef);
        eagerStorer.commit();
    }

    public List<PatientHistoryEntity> findHistoryByPatient(PatientEntity patient) {
        return patientsHistoryRef.get().getOrDefault(patient, new ArrayList<>());
    }

    public Boolean existsHistoryFor(PatientEntity patient) {
        return patientsHistoryRef.get().containsKey(patient);
    }

    public void addToBlocked(PatientEntity patient) {
        var blockedPatients = blockedPatientsRef.get();
        blockedPatients.add(patient);
        storageManager.store(blockedPatients);
    }

    public void removeFromBlocked(PatientEntity patient) {
        var blockedPatients = blockedPatientsRef.get();
        blockedPatients.remove(patient);
        storageManager.store(blockedPatients);
    }

    public void clearActiveVisitSession() {
        activeVisitSession = Optional.empty();
        var eagerStorer = storageManager.createEagerStorer();
        eagerStorer.store(this);
        eagerStorer.commit();
    }

    public Optional<VisitSessionEntity> findVisitSessionByDate(LocalDate date) {
        var visitSessions = visitSessionsHistory.get().filterToList(vs -> vs.getDate() == date);
        activeVisitSession.filter(vs -> vs.getDate() == date).ifPresent(visitSessions::add);
        if (visitSessions.isEmpty())
            return Optional.empty();

        var maxAppointmentsVisitSession = visitSessions[0];
        for (VisitSessionEntity vs : visitSessions) {
            if (vs.getAppointments().size() > maxAppointmentsVisitSession.getAppointments().size())
                maxAppointmentsVisitSession = vs;
        }
        return Optional.of(maxAppointmentsVisitSession);
    }

    public void setStorageManager(StorageManager storageManager) {
        this.storageManager = storageManager;
    }
}

package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.patient.Patient;
import ir.milad.DocVisitApp.domain.patient.PatientHistoryRow;
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

    private Optional<VisitSession> activeVisitSession = Optional.empty();
    private Lazy<List<VisitSession>> visitSessionsHistory = Lazy.Reference(new ArrayList<>());
    private Lazy<Map<Patient, PatientHistoryRow>> patientsHistory = Lazy.Reference(new HashMap<>());
    private Lazy<List<Patient>> blockedPatients = Lazy.Reference(new ArrayList<>());

    public void setActiveVisitSession(VisitSession visitSession) {
        Objects.requireNonNull(visitSession, "Database.setCurrentActiveVisitSession can't accept null visitSession");
        activeVisitSession.ifPresent(session -> visitSessionsHistory.get().add(session));
        activeVisitSession = Optional.of(visitSession);
        storageManager.store(activeVisitSession);
        storageManager.store(visitSessionsHistory);
    }

    public boolean hasActiveVisitSession(LocalDate date) {
        return activeVisitSession
                .map(visitSession -> visitSession.getDate().equals(date))
                .orElse(false);
    }

    public void updateActiveVisitSession() {
        storageManager.store(activeVisitSession);
    }

    public Optional<VisitSession> getActiveSession(LocalDateTime dateTime) {
        if (activeVisitSession.isEmpty())
            return Optional.empty();

        var vs = activeVisitSession.get();
        if (vs.getDate().equals(dateTime.toLocalDate()) &&
                dateTime.toLocalTime().equals(vs.getFromTime()) || dateTime.toLocalTime().isAfter(vs.getFromTime()) &&
                dateTime.toLocalTime().equals(vs.getToTime()) || dateTime.toLocalTime().isBefore(vs.getToTime()))
            return activeVisitSession;
        return Optional.empty();
    }

    @PreDestroy
    public void onDestroy() {
        storageManager.shutdown();
    }
}

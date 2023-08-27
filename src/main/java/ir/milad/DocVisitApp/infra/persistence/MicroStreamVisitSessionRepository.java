package ir.milad.DocVisitApp.infra.persistence;

import ir.milad.DocVisitApp.domain.visit_session.VisitSession;
import ir.milad.DocVisitApp.domain.visit_session.VisitSessionRepository;
import one.microstream.concurrency.XThreads;
import one.microstream.persistence.types.Storer;
import one.microstream.storage.embedded.types.EmbeddedStorage;
import one.microstream.storage.embedded.types.EmbeddedStorageManager;
import org.springframework.stereotype.Repository;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public class MicroStreamVisitSessionRepository implements VisitSessionRepository {

    private static final String DB_NAME = "visitTimeTables";
    private VisitTimeTables db;
    private final EmbeddedStorageManager storageManager = EmbeddedStorage.start(db, Paths.get(DB_NAME));
    private final Storer eagerStorer;

    public MicroStreamVisitSessionRepository() {
        if (storageManager.root() == null) {
            db = new VisitTimeTables();
            storageManager.setRoot(db);
            storageManager.storeRoot();
        }
        else
            db = (VisitTimeTables) storageManager.root();

        eagerStorer = storageManager.createEagerStorer();
        Runtime.getRuntime().addShutdownHook(new Thread(storageManager::shutdown));
    }

    @Override
    public void save(VisitSession visitSession) {
        XThreads.executeSynchronized(() -> {
            db.visitSessions.add(visitSession);
            storageManager.store(db.visitSessions);
        });

    }

    @Override
    public void update() {
        XThreads.executeSynchronized(() -> {
            eagerStorer.store(db);
            eagerStorer.commit();
        });
    }

    @Override
    public Optional<VisitSession> findById(UUID id) {
        return db.visitSessions
                .stream()
                .filter(vs -> vs.getId().compareTo(id) == 0)
                .findFirst();
    }

    @Override
    public Optional<VisitSession> findByDateAndFromTimeAndToTime(LocalDate date, LocalTime fromTime, LocalTime toTime) {
        return db.visitSessions
                .stream()
                .filter(vs -> vs.getDate().isEqual(date) && vs.getFromTime().equals(fromTime) && vs.getToTime().equals(toTime))
                .findFirst();
    }

    @Override
    public Optional<VisitSession> findForTodayAndNow() {
        var now = LocalTime.now().withSecond(0).withNano(0);
        return db.visitSessions
                .stream()
                .filter(vs -> vs.getDate().isEqual(LocalDate.now()) &&
                        now.equals(vs.getFromTime()) || now.isAfter(vs.getFromTime()) &&
                        now.equals(vs.getToTime()) || now.isBefore(vs.getToTime())
                )
                .findFirst();
    }

}

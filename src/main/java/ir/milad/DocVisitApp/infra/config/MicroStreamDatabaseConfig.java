package ir.milad.DocVisitApp.infra.config;

import ir.milad.DocVisitApp.infra.persistence.Database;
import ir.milad.DocVisitApp.infra.persistence.DatabaseV2;
import one.microstream.storage.embedded.configuration.types.EmbeddedStorageConfiguration;
import one.microstream.storage.types.StorageManager;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

@Configuration
public class MicroStreamDatabaseConfig {

    @Value("${app.previous.database.dir}")
    private String previousDatabaseDir;

    @Value("${app.current.database.dir}")
    private String currentDatabaseDir;

    @Bean(destroyMethod = "shutdown")
    @Primary
    public StorageManager appStorageManager() {
        Database previousRoot = null;
        Path prevDatabasePath = Path.of(previousDatabaseDir);
        StorageManager prevDatabaseManager = null;
        if (Files.exists(prevDatabasePath) && Files.isDirectory(prevDatabasePath)) {
            var storageFoundation = EmbeddedStorageConfiguration.Builder()
                    .setChannelCount(2)
                    .setStorageDirectory(previousDatabaseDir)
                    .createEmbeddedStorageFoundation();
            prevDatabaseManager = storageFoundation.start();
            previousRoot = (Database) prevDatabaseManager.root();
        }

        var storageFoundation = EmbeddedStorageConfiguration.Builder()
                .setChannelCount(2)
                .setStorageDirectory(currentDatabaseDir)
                .createEmbeddedStorageFoundation();

        StorageManager storageManager = storageFoundation.start();

        // Check Root available within StorageManager
        DatabaseV2 root = (DatabaseV2) storageManager.root();
        if (root == null) {
            if (previousRoot == null)
                root = new DatabaseV2();
            else
                root = DatabaseV2.from(previousRoot);
        }

        root.setStorageManager(storageManager);
        storageManager.setRoot(root);
        storageManager.storeRoot();

        if (prevDatabaseManager != null) {
            prevDatabaseManager.shutdown();
            FileUtils.moveDirectoryToDirectory(
                    new File(previousDatabaseDir),
                    new File("__" + previousDatabaseDir),
                    true
            );
        }
        
        return storageManager;
    }
}

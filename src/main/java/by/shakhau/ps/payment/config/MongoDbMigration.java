package by.shakhau.ps.payment.config;

import jakarta.annotation.PostConstruct;
import liquibase.Liquibase;
import liquibase.database.DatabaseFactory;
import liquibase.exception.LiquibaseException;
import liquibase.ext.mongodb.database.MongoLiquibaseDatabase;
import liquibase.resource.ClassLoaderResourceAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoDbMigration {

    private final LiquibaseProperties liquibaseProperties;

    @PostConstruct
    public void init() throws LiquibaseException {
        try (MongoLiquibaseDatabase database = (MongoLiquibaseDatabase) DatabaseFactory.getInstance()
                .openDatabase(liquibaseProperties.getUrl(), liquibaseProperties.getUser(), liquibaseProperties.getPassword(), null, null)) {

            try (var liquibase = new Liquibase(liquibaseProperties.getChangeLog(), new ClassLoaderResourceAccessor(), database)) {
                liquibase.update("");
            }
        }
    }
}

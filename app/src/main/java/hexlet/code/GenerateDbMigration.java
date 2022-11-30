package hexlet.code;

import io.ebean.dbmigration.DbMigration;
import io.ebean.annotation.Platform;
import java.io.IOException;

public final class GenerateDbMigration {

    public static void main(String[] args) throws IOException {
        DbMigration dbMigration = DbMigration.create();

        dbMigration.addPlatform(Platform.POSTGRES, "dbMigration/postgres");
        dbMigration.addPlatform(Platform.H2, "dbMigration/h2");

        dbMigration.generateMigration();
    }
}

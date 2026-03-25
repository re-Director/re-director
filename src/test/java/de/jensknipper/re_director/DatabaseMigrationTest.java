package de.jensknipper.re_director;

import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {"spring.liquibase.enabled=false"})
class DatabaseMigrationTest {

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @Autowired
  DataSource dataSource;
  @Autowired
  DSLContext dsl;

  @Test
  void shouldMigrateFromV1ToLatest_withExistingData() throws Exception {

    try (Connection connection = dataSource.getConnection()) {

      Liquibase liquibase = new Liquibase(
        "db/changelog/db.changelog-master.yaml",
        new ClassLoaderResourceAccessor(),
        new JdbcConnection(connection)
      );

      liquibase.update("v0004", new Contexts(), new LabelExpression());

      dsl.execute("""
                INSERT INTO redirects (source, target)
                VALUES ('example-source', 'example-target')
            """);

      liquibase.update(new Contexts(), new LabelExpression());
    }

    var record = dsl.fetchOne("SELECT * FROM redirects WHERE id = 1");

    assertThat(record).isNotNull();
  }
}
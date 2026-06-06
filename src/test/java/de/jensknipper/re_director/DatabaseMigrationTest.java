package de.jensknipper.re_director;

import static org.assertj.core.api.Assertions.assertThat;

import de.jensknipper.re_director.common.db.RedirectHttpStatusCode;
import de.jensknipper.re_director.common.db.Status;
import de.jensknipper.re_director.manage_redirects.ManageRedirectsService;
import de.jensknipper.re_director.manage_redirects.Redirect;
import java.sql.Connection;
import java.util.UUID;
import javax.sql.DataSource;
import liquibase.Contexts;
import liquibase.LabelExpression;
import liquibase.Liquibase;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;
import org.jooq.DSLContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {"spring.liquibase.enabled=false"})
class DatabaseMigrationTest {

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @Autowired DataSource dataSource;
  @Autowired DSLContext dsl;
  @Autowired ManageRedirectsService manageRedirectsService;

  @Test
  void migrationWithData() throws Exception {

    try (Connection connection = dataSource.getConnection()) {

      Liquibase liquibase =
          new Liquibase(
              "db/changelog/db.changelog-master.yaml",
              new ClassLoaderResourceAccessor(),
              new JdbcConnection(connection));

      liquibase.update("v0004", new Contexts(), new LabelExpression());

      dsl.execute(
          """
                INSERT INTO redirects (source, target, status, http_status_code)
                VALUES ('example-source', 'example-target', 'ACTIVE', 'FOUND')
            """);

      liquibase.update(new Contexts(), new LabelExpression());
    }

    Page<Redirect> records =
        manageRedirectsService.findAllFiltered(null, null, null, Pageable.ofSize(10));

    assertThat(records).hasSize(1);
    assertThat(records.getContent().getFirst().source()).isEqualTo("example-source");
    assertThat(records.getContent().getFirst().target()).isEqualTo("example-target");
    assertThat(records.getContent().getFirst().status()).isEqualTo(Status.ACTIVE);
    assertThat(records.getContent().getFirst().httpStatusCode())
        .isEqualTo(RedirectHttpStatusCode.HTTP_302_FOUND);
  }
}

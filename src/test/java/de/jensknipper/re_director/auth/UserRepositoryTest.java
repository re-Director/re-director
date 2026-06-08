package de.jensknipper.re_director.auth;

import static de.jensknipper.re_director.database.tables.Users.USERS;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import java.util.UUID;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class UserRepositoryTest {

  @Autowired private DSLContext dsl;
  @Autowired private UserRepository userRepository;

  @DynamicPropertySource
  static void overrideProps(DynamicPropertyRegistry registry) {
    String uniqueDb = "jdbc:sqlite:file::memdb-" + UUID.randomUUID() + ":?mode=memory&cache=shared";
    registry.add("spring.datasource.url", () -> uniqueDb);
  }

  @BeforeEach
  void cleanup() {
    dsl.deleteFrom(USERS).execute();
  }

  @Test
  void createAndSelectShouldWork() {
    // given
    String username = "username";
    // when
    userRepository.createUser(username, "passwordHash", true);
    Optional<User> result = userRepository.findByUsername(username);

    // then
    assertThat(result).isPresent();
    assertThat(result.get().username()).isEqualTo(username);
    assertThat(result.get().passwordHash()).isEqualTo("passwordHash");
    assertThat(result.get().enabled()).isTrue();
  }

  @Test
  void countShouldReturnNumberOfUsers() {
    // given
    long count = userRepository.count();
    assertThat(count).isZero();

    // when
    userRepository.createUser("username", "passwordHash", true);

    // then
    count = userRepository.count();
    assertThat(count).isOne();
  }
}

package de.jensknipper.re_director.auth;

import static de.jensknipper.re_director.database.tables.Users.USERS;

import java.util.Objects;
import java.util.Optional;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserRepository {
  private final DSLContext dsl;

  public UserRepository(DSLContext dsl) {
    this.dsl = dsl;
  }

  public Optional<User> findByUsername(String username) {
    return Optional.ofNullable(
        dsl.selectFrom(USERS).where(USERS.USERNAME.eq(username)).fetchOneInto(User.class));
  }

  public long count() {
    return dsl.fetchCount(USERS);
  }

  public int createUser(String username, String passwordHash, boolean enabled) {
    return Objects.requireNonNull(
            dsl.insertInto(USERS)
                .columns(USERS.USERNAME, USERS.PASSWORD_HASH, USERS.ENABLED)
                .values(username, passwordHash, enabled)
                .returningResult(USERS.ID)
                .fetchOne())
        .getValue(USERS.ID);
  }
}

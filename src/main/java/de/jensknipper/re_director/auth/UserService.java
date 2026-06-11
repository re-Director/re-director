package de.jensknipper.re_director.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public boolean hasUsers() {
    return userRepository.count() > 0;
  }

  public int createUser(String username, String password) {
    String hash = passwordEncoder.encode(password);
    if (hash == null) {
      // this should not happen
      throw new NullPointerException("Password Hash turned out null!");
    }
    return userRepository.createUser(username, hash, true);
  }
}

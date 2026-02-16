package staryhroft.templog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import staryhroft.templog.entity.User;
import staryhroft.templog.entity.enums.Role;
import staryhroft.templog.repository.UserRepository;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("No users found, creating default users...");

            User admin = User.builder()
                    .username("admin")
                    .password(passwordEncoder.encode("123"))
                    .role(Role.ADMIN)
                    .build();

            User user = User.builder()
                    .username("user")
                    .password(passwordEncoder.encode("123"))
                    .role(Role.USER)
                    .build();

            userRepository.save(admin);
            userRepository.save(user);

            log.info("Default users created: admin (ADMIN), user (USER)");
        } else {
            log.debug("Users already exist, skipping initialization");
        }
    }
}

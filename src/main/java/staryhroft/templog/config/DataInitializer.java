package staryhroft.templog.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import staryhroft.templog.entity.User;
import staryhroft.templog.entity.enums.Role;
import staryhroft.templog.repository.UserRepository;
import staryhroft.templog.service.UserService;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {
    private final UserService userService;

    @Override
    public void run(String... args) {
        log.info("Начинается инициализация данных...");
        userService.initializeDefaultUsers();
        log.info("Инициализация данных завершена.");
    }
}

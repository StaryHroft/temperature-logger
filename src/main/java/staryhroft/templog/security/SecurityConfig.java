package staryhroft.templog.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET,"/api/cities").permitAll()     //список городов доступен всем
                        .requestMatchers(HttpMethod.GET,"/api/cities/{name}").hasAnyRole("USER", "ADMIN")     //город для юзеров и админов
                        .requestMatchers(HttpMethod.POST,"/api/cities/{name}/favorite").hasAnyRole("USER", "ADMIN")     //добавление в избранное для юзеров и админов
                        .requestMatchers(HttpMethod.DELETE,"/api/cities/{name}/favorite").hasAnyRole("USER", "ADMIN")     //удаление из избранного для юзеров и админов
                        .requestMatchers(HttpMethod.GET,"/api/cities/count").hasRole("ADMIN")      //количество только админ
                        .requestMatchers(HttpMethod.DELETE,"/api/cities/{name}").hasRole("ADMIN")     //удаление города (DELETE) только админ
                        .requestMatchers(HttpMethod.DELETE,"/api/cities").hasRole("ADMIN")        //удаление всех городов (DELETE) только админ
                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> httpBasic.init(http));
    return http.build();
    }
}

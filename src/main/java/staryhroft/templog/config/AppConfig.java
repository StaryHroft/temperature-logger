package staryhroft.templog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {


    @Bean
    public RestTemplate restTemplate(){
        return new RestTemplate();
    }
}

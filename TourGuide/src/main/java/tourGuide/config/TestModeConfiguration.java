package tourGuide.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.tripmaster.tourguide")
@Data
public class TestModeConfiguration {
    // TODO voir pourquoi l'externalisation ne fonctionne pas, c'est toujours à false même dans les tests
    private boolean testMode;
}
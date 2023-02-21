package tourGuide.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com.tripmaster.tourguide")
@Data
public class TestModeConfiguration {
    private boolean testMode;
}
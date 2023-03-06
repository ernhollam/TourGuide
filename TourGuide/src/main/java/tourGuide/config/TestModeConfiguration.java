package tourGuide.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("application.properties")
@Data
public class TestModeConfiguration {
    @Value("${com.tripmaster.tourguide.testmode}")
    private boolean testMode;
}
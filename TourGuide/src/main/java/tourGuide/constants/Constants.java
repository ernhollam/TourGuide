package tourGuide.constants;

import java.util.concurrent.TimeUnit;

public class Constants {
    public static final double STATUTE_MILES_PER_NAUTICAL_MILE = 1.15077945;
    public static final int DEFAULT_PROXIMITY_BUFFER = 10;
    public static final long TRACKING_POLLING_INTERVAL = TimeUnit.MINUTES.toSeconds(5);
}
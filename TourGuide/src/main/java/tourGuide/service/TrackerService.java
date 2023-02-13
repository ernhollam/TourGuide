package tourGuide.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourGuide.tracker.Tracker;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class TrackerService {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    private final TourGuideService tourGuideService;

    final UserService userService;

    private Tracker tracker;

    public TrackerService(TourGuideService tourGuideService, UserService userService) {
        this.tourGuideService = tourGuideService;
        this.userService      = userService;
        tracker               = new Tracker(tourGuideService, userService);
        executorService.submit(tracker);
        addShutDownHook();
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        tracker.stopTracking();
        executorService.shutdownNow();
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(tracker::stopTracking));
    }
}
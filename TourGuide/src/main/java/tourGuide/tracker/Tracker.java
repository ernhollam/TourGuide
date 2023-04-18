package tourGuide.tracker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import tourGuide.constants.TourGuideConstants;
import tourGuide.model.User;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Slf4j
public class Tracker extends Thread {

    private final TourGuideService tourGuideService;

    final   UserService userService;
    private boolean     stop = false;

    public Tracker(TourGuideService tourGuideService, UserService userService) {
        this.tourGuideService = tourGuideService;
        this.userService      = userService;
    }

    /**
     * Assures to shut down the Tracker thread
     */
    public void stopTracking() {
        stop = true;
    }

    @Override
    public void run() {
        StopWatch stopWatch = new StopWatch();
        while (true) {
            if (Thread.currentThread().isInterrupted() || stop) {
                log.debug("Tracker stopping");
                break;
            }

            List<User> users = userService.getAllUsers();
            log.debug("Begin Tracker. Tracking " + users.size() + " users.");
            stopWatch.start();
            users.parallelStream().forEach(user -> {
                try {
                    tourGuideService.trackUserLocation(user).get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("Error while tracking user {}", user.getUserName(), e);
                }
            });
            stopWatch.stop();
            log.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
            stopWatch.reset();
            try {
                log.debug("Tracker sleeping");
                TimeUnit.SECONDS.sleep(TourGuideConstants.TRACKING_POLLING_INTERVAL);
            } catch (InterruptedException e) {
                break;
            }
        }

    }
}
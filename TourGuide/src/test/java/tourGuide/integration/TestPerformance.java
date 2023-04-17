package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.*;

import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class TestPerformance {

    /*
     * A note on performance improvements:
     *
     *     The number of users generated for the high volume tests can be easily adjusted via this method:
     *
     *     		InternalTestHelper.setInternalUserNumber(100000);
     *
     *
     *     These tests can be modified to suit new solutions, just as long as the performance metrics
     *     at the end of the tests remains consistent.
     *
     *     These are performance metrics that we are trying to hit:
     *
     *     highVolumeTrackLocation: 100,000 users within 15 minutes:
     *     		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     *
     *     highVolumeGetRewards: 100,000 users within 20 minutes:
     *          assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
     */
    TourGuideService mapTourGuideService;

    RewardsService mapRewardsService;
    UserService    mapUserService;
    private final GpsUtil               gpsUtil               = new GpsUtil();
    private       StopWatch             stopWatch;
    @Before
    public void setUp() {
        Locale.setDefault(Locale.US);
        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalTestHelper.setInternalUserNumber(1000);
        stopWatch = new StopWatch();

        // set up services
        mapRewardsService = new MapRewardsService(gpsUtil, new RewardCentral());
        mapUserService      = new MapUserService();
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);

    }
    //@Ignore
    @Test
    public void highVolumeTrackLocation() {

        List<User> allUsers = mapUserService.getAllUsers();

        stopWatch.start();
        allUsers.parallelStream().forEach((user) -> {
            try {
                mapTourGuideService.trackUserLocation(user).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        });
        stopWatch.stop();

        // make sure each user has at least 4 visited locations
        // (3 locations added during initialization and 1 during test)
        allUsers
                .parallelStream()
                .forEach(user -> assertTrue(user.getVisitedLocations().size() >= 4));
        // trackerService.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    //@Ignore
    @Test
    public void highVolumeGetRewards() {// add first attraction to all test users location
        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers   = mapUserService.getAllUsers();
        allUsers.parallelStream().forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        stopWatch.start();
        // use parallel stream to add rewards to users as it is used when calculateRewards is called
        allUsers.parallelStream().forEach(user -> {
            try {
                mapRewardsService.calculateRewards(user);
            } catch (ExecutionException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        stopWatch.stop();

        // make sure each user is rewarded for visiting the attraction
        allUsers
                .parallelStream()
                .forEach(user -> assertTrue(user.getUserRewards().size() > 0));

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.apache.commons.lang3.time.StopWatch;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.config.TestModeConfiguration;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.*;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Import(TestModeConfiguration.class)
public class PerformanceIT {

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
    TrackerService trackerService;
    private final GpsUtil          gpsUtil = new GpsUtil();
    private       StopWatch        stopWatch;
    private final TestModeConfiguration testModeConfiguration = new TestModeConfiguration();
    @Before
    public void setUp() {
        // Users should be incremented up to 100,000, and test finishes within 15 minutes
        InternalTestHelper.setInternalUserNumber(100);
        stopWatch = new StopWatch();

        // set up services
        mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
        mapUserService      = new MapUserService(testModeConfiguration);
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);

        trackerService = new TrackerService(mapTourGuideService, mapUserService);
    }
    //@Ignore
    @Test
    public void highVolumeTrackLocation() {

        List<User> allUsers = mapUserService.getAllUsers();

        stopWatch.start();

        for (User user : allUsers) {
            mapTourGuideService.trackUserLocation(user);
        }
        stopWatch.stop();
        trackerService.stopTracking();

        System.out.println("highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

    //@Ignore
    @Test
    public void highVolumeGetRewards() {
        stopWatch.start();

        Attraction attraction = gpsUtil.getAttractions().get(0);
        List<User> allUsers   = mapUserService.getAllUsers();
        allUsers.forEach(u -> u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date())));

        allUsers.forEach(u -> mapRewardsService.calculateRewards(u));

        for (User user : allUsers) {
            assertTrue(user.getUserRewards().size() > 0);
        }
        stopWatch.stop();
        trackerService.stopTracking();

        System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds.");
        assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
    }

}
package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.config.TestModeConfiguration;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.service.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
@Import(TestModeConfiguration.class)
public class MapRewardsServiceIT {
    private final GpsUtil               gpsUtil               = new GpsUtil();
    @Autowired
    private TestModeConfiguration testModeConfiguration;
    RewardsService   mapRewardsService;
    UserService mapUserService;
    TourGuideService mapTourGuideService;
    TrackerService   trackerService;

    @Before
    public void setServices() {
        mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
        mapUserService = new MapUserService(testModeConfiguration);
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);
    }

    @Test
    public void userGetRewards() {
        //Start tracker and reset number of users
        trackerService = new TrackerService(mapTourGuideService, mapUserService);
        InternalTestHelper.setInternalUserNumber(0);
        // create new user
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        // get first attraction in list and add its location to user's visited locations
        Attraction attraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        mapTourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        trackerService.stopTracking();
        // user must have the reward for the first attraction
        assertEquals(1, userRewards.size());
    }

    @Test
    public void isWithinAttractionProximity() {
        Attraction attraction = gpsUtil.getAttractions().get(0);
        assertTrue(mapRewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    @Test
    public void nearAllAttractions() {
        // GIVEN a test user and setting proximity to maximal value
        InternalTestHelper.setInternalUserNumber(1);
        trackerService = new TrackerService(mapTourGuideService, mapUserService);
        mapRewardsService.setProximityBuffer(Integer.MAX_VALUE);
        // WHEN calculating the rewards for the test user
        mapRewardsService.calculateRewards(mapUserService.getAllUsers().get(0));
        List<UserReward> userRewards = mapTourGuideService.getUserRewards(mapUserService.getAllUsers().get(0));
        trackerService.stopTracking();
        //THEN user must have rewards for all attractions
        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
    }

}
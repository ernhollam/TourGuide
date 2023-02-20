package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.service.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@ExtendWith(SpringExtension.class)
public class MapRewardsServiceIT {
    private final GpsUtil gpsUtil = new GpsUtil();
    RewardsService   mapRewardsService;
    TourGuideService mapTourGuideService;
    TrackerService   trackerService;


    @Test
    public void userGetRewards() {
        mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService);
        //Start tracker and reset number of users
        trackerService = new TrackerService(mapTourGuideService, new MapUserService());
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
        mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService);
        Attraction attraction = gpsUtil.getAttractions().get(0);
        assertTrue(mapRewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    @Test
    public void nearAllAttractions() {
        mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService);
        // GIVEN a test user and setting proximity to maximal value
        InternalTestHelper.setInternalUserNumber(1);
        UserService userService = new MapUserService();
        trackerService = new TrackerService(mapTourGuideService, userService);
        mapRewardsService.setProximityBuffer(Integer.MAX_VALUE);
        // WHEN calculating the rewards for the test user
        mapRewardsService.calculateRewards(userService.getAllUsers().get(0));
        List<UserReward> userRewards = mapTourGuideService.getUserRewards(userService.getAllUsers().get(0));
        trackerService.stopTracking();
        //THEN user must have rewards for all attractions
        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
    }

}
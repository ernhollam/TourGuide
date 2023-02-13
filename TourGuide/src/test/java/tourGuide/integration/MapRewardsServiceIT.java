package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.service.RewardsService;
import tourGuide.service.UserService;
import tourGuide.service.MapTourGuideService;
import tourGuide.service.TrackerService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@RunWith(SpringRunner.class)
public class MapRewardsServiceIT {

    @Autowired
    private RewardsService      rewardsService;
    @Autowired
    private MapTourGuideService mapTourGuideService;
    @Autowired
    private TrackerService trackerService;
    @Autowired
    private UserService    userService;

    private final GpsUtil gpsUtil = new GpsUtil();

    @Test
    public void userGetRewards() {
        InternalTestHelper.setInternalUserNumber(0);
        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        Attraction attraction = gpsUtil.getAttractions().get(0);
        user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
        mapTourGuideService.trackUserLocation(user);
        List<UserReward> userRewards = user.getUserRewards();
        trackerService.stopTracking();
        assertEquals(1, userRewards.size());
    }

    @Test
    public void isWithinAttractionProximity() {
        Attraction attraction = gpsUtil.getAttractions().get(0);
        assertTrue(rewardsService.isWithinAttractionProximity(attraction, attraction));
    }

    //@Ignore // Needs fixed - can throw ConcurrentModificationException
    @Test
    public void nearAllAttractions() {
        rewardsService.setProximityBuffer(Integer.MAX_VALUE);

        InternalTestHelper.setInternalUserNumber(1);

        rewardsService.calculateRewards(userService.getAllUsers().get(0));
        List<UserReward> userRewards = mapTourGuideService.getUserRewards(userService.getAllUsers().get(0));
        trackerService.stopTracking();

        assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
    }

}
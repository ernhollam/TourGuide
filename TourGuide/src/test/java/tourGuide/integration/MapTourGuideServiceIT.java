package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.MapRewardsService;
import tourGuide.service.MapTourGuideService;
import tourGuide.service.TrackerService;
import tripPricer.Provider;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@RunWith(SpringRunner.class)
public class MapTourGuideServiceIT {

    /**
     * Class under test.
     */
    @Autowired
    private MapTourGuideService mapTourGuideService;

    @Autowired
    private MapRewardsService mapRewardsService;
    @Autowired
    private TrackerService    trackerService;
    private GpsUtil gpsUtil = new GpsUtil();

    @BeforeAll
    public static void setUp() {
        InternalTestHelper.setInternalUserNumber(0);
    }

    @Test
    public void getUserLocation() {
        User            user            = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = mapTourGuideService.trackUserLocation(user);
        trackerService.stopTracking();
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    public void trackUser() {
        User            user            = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = mapTourGuideService.trackUserLocation(user);

        trackerService.stopTracking();

        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    //@Ignore // Not yet implemented
    @Test
    public void getNearbyAttractions() {

        User            user            = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        VisitedLocation visitedLocation = mapTourGuideService.trackUserLocation(user);

        List<Attraction> attractions = mapTourGuideService.getNearByAttractions(visitedLocation);

        trackerService.stopTracking();

        assertEquals(5, attractions.size());
    }

    @Test
    public void getTripDeals() {

        User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

        List<Provider> providers = mapTourGuideService.getTripDeals(user);

        trackerService.stopTracking();

        assertEquals(10, providers.size());
    }


}
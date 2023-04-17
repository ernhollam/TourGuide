package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.constants.TourGuideConstants;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.model.ViewModel.NearbyAttractionViewModel;
import tourGuide.service.*;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class MapTourGuideServiceIT {

    /**
     * Class under test.
     */
    TourGuideService mapTourGuideService;

    RewardsService mapRewardsService;
    UserService    mapUserService;
    TrackerService trackerService;
    private final GpsUtil               gpsUtil               = new GpsUtil();

    User            user;
    User            user2;
    VisitedLocation visitedLocation;
    VisitedLocation visitedLocation2;


    @Before
    public void setServices() throws ExecutionException, InterruptedException {
        // reset number of test users
        InternalTestHelper.setInternalUserNumber(0);
        // set up services
        mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
        mapUserService      = new MapUserService();
        mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);

        trackerService = new TrackerService(mapTourGuideService, mapUserService);
        // create test user and add one visited location
        user            = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        user2           = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        visitedLocation = mapTourGuideService.trackUserLocation(user).get();
        visitedLocation2 = mapTourGuideService.trackUserLocation(user2).get();
        mapUserService.addUser(user);
        mapUserService.addUser(user2);
    }

    @Test
    public void getUserLocation() {
        trackerService.stopTracking();
        assertEquals(visitedLocation.userId, user.getUserId());
    }

    @Test
    public void trackUser() {
        trackerService.stopTracking();
        assertEquals(user.getUserId(), visitedLocation.userId);
    }

    @Test
    public void getAttractionsWithinProximityRange() {
        // TODO set proximity buffer to max value, get 1st attraction and set visitedLocation to attraction's location, attraction must be nearby visitedLocation

    }
    // @Ignore // Not yet implemented
    @Test
    public void getNearByAttractions() throws ExecutionException, InterruptedException {
        List<NearbyAttractionViewModel> nearByAttractions = mapTourGuideService.getNearByAttractions(user);
        trackerService.stopTracking();

        assertEquals(TourGuideConstants.NUMBER_OF_NEARBY_ATTRACTIONS, nearByAttractions.size());
        assertTrue(nearByAttractions.get(0).getDistanceInMiles() <= nearByAttractions.get(1).getDistanceInMiles());
        assertTrue(nearByAttractions.get(1).getDistanceInMiles() <= nearByAttractions.get(2).getDistanceInMiles());
        assertTrue(nearByAttractions.get(2).getDistanceInMiles() <= nearByAttractions.get(3).getDistanceInMiles());
        assertTrue(nearByAttractions.get(3).getDistanceInMiles() <= nearByAttractions.get(4).getDistanceInMiles());
    }

    @Test
    public void getTripDeals() {
        List<Provider> providers = mapTourGuideService.getTripDeals(user);
        trackerService.stopTracking();

        assertEquals(5, providers.size());
    }

    @Test
    public void getAllCurrentLocations() throws ExecutionException, InterruptedException {
        // GIVEN two users
        // WHEN calling getAllCurrentLocations()
        Map<UUID, Location> result = mapTourGuideService.getAllCurrentLocations();
        // THEN result must contain a map with two users and their locations
        assertEquals(visitedLocation.location, result.get(user.getUserId()));
        assertEquals(visitedLocation2.location, result.get(user2.getUserId()));
    }
}
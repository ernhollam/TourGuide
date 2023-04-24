package tourGuide.integration;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.junit4.SpringRunner;
import rewardCentral.RewardCentral;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.MapRewardsService;
import tourGuide.service.MapTourGuideService;
import tourGuide.service.MapUserService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TrackerService;
import tourGuide.service.UserService;

import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class TestRewardsService {
	private final GpsUtil gpsUtil = new GpsUtil();
	RewardsService   mapRewardsService;
	TourGuideService mapTourGuideService;
	UserService      mapUserService;

	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		InternalTestHelper.setInternalUserNumber(0);
		mapRewardsService = new MapRewardsService(gpsUtil, new RewardCentral());
	}

	@Test
	public void userGetRewards() throws InterruptedException {
		InternalTestHelper.setInternalUserNumber(1);
		mapUserService      = new MapUserService(true);
		mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);
		TrackerService trackerService = new TrackerService(mapTourGuideService, mapUserService);
		User           testUser       = mapUserService.getAllUsers().get(0);
		testUser.clearVisitedLocations();
		testUser.addToVisitedLocations(
				new VisitedLocation(testUser.getUserId(), gpsUtil.getAttractions().get(0), new Date()));
		boolean continueTracking = true;
		while (continueTracking) {
			if (!testUser.getUserRewards().isEmpty()) {
				continueTracking = false;
			}
			TimeUnit.MILLISECONDS.sleep(200);
		}
		trackerService.stopTracking();

		// user must have the reward for the first attraction
		assertEquals(1, testUser.getUserRewards().size());
	}

	@Test
	public void nearAllAttractions() throws InterruptedException {
		// GIVEN maximum value for distance between a location and an attraction
		mapRewardsService.setProximityBuffer(Integer.MAX_VALUE);

		InternalTestHelper.setInternalUserNumber(1);
		mapUserService      = new MapUserService(true);
		mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);
		// WHEN tracker started
		TrackerService trackerService = new TrackerService(mapTourGuideService, mapUserService);
		User           testUser       = mapUserService.getAllUsers().get(0);
		while (mapTourGuideService.getUserRewards(testUser).size() < gpsUtil.getAttractions().size()) {
			TimeUnit.MILLISECONDS.sleep(200);
		}
		trackerService.stopTracking();
		//THEN
		assertEquals(gpsUtil.getAttractions().size(), testUser.getUserRewards().size());
	}

	@Test
	public void isWithinAttractionProximity() {
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertTrue(mapRewardsService.isWithinAttractionProximity(attraction, attraction));
	}

	@Test
	public void isNotWithinAttractionProximity() {
		Attraction attraction = gpsUtil.getAttractions().get(0);
		assertFalse(mapRewardsService.isWithinAttractionProximity(attraction, gpsUtil.getAttractions().get(10)));
	}
}
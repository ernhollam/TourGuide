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
import tourGuide.service.MapRewardsService;
import tourGuide.service.MapTourGuideService;
import tourGuide.service.MapUserService;
import tourGuide.service.RewardsService;
import tourGuide.service.TourGuideService;
import tourGuide.service.TrackerService;
import tourGuide.service.UserService;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringRunner.class)
public class TestPerformance {

	TourGuideService mapTourGuideService;
	RewardsService   mapRewardsService;
	UserService      mapUserService;
	private final GpsUtil gpsUtil = new GpsUtil();

	@Before
	public void setUp() {
		Locale.setDefault(Locale.US);
		// Users should be incremented up to 100,000, and test finishes within 15 minutes
		InternalTestHelper.setInternalUserNumber(100000);
		mapUserService      = new MapUserService(true);
		mapRewardsService   = new MapRewardsService(gpsUtil, new RewardCentral());
		mapTourGuideService = new MapTourGuideService(gpsUtil, mapRewardsService, mapUserService);
	}

	@Test
	public void highVolumeTrackLocation() throws InterruptedException {
		// begin tracking
		TrackerService trackerService = new TrackerService(mapTourGuideService, mapUserService);

		List<User> allUsers     = mapUserService.getAllUsers();
		List<UUID> locatedUsers = new ArrayList<>();
		StopWatch  stopWatch    = new StopWatch();
		stopWatch.start();
		while (locatedUsers.size() < allUsers.size()) {
			for (User user : allUsers) {
				if (!locatedUsers.contains(user.getUserId()) && user.getVisitedLocations().size() > 3) {
					locatedUsers.add(user.getUserId());
				}
			}
			TimeUnit.MILLISECONDS.sleep(200);
		}
		assertEquals(locatedUsers.size(), allUsers.size());
		stopWatch.stop();
		trackerService.stopTracking();

		System.out.println(
				"highVolumeTrackLocation: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
						+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(15) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}

	@Test
	public void highVolumeGetRewards() throws InterruptedException {
		StopWatch stopWatch = new StopWatch();

		Attraction attraction    = gpsUtil.getAttractions().get(0);
		List<User> allUsers      = mapUserService.getAllUsers();
		List<UUID> rewardedUsers = new ArrayList<>();
		allUsers.forEach(u -> {
			u.clearVisitedLocations();
			u.addToVisitedLocations(new VisitedLocation(u.getUserId(), attraction, new Date()));
		});

		stopWatch.start();
		// begin tracking
		TrackerService trackerService = new TrackerService(mapTourGuideService, mapUserService);
		while (rewardedUsers.size() < allUsers.size()) {
			for (User user : allUsers) {
				if (!rewardedUsers.contains(user.getUserId()) && user.getUserRewards().size() > 0) {
					rewardedUsers.add(user.getUserId());
				}
			}
			TimeUnit.MILLISECONDS.sleep(200);
		}

		assertEquals(rewardedUsers.size(), allUsers.size());
		stopWatch.stop();
		trackerService.stopTracking();

		System.out.println("highVolumeGetRewards: Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime())
				+ " seconds.");
		assertTrue(TimeUnit.MINUTES.toSeconds(20) >= TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()));
	}
}
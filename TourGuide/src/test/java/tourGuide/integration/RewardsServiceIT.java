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
import tourGuide.service.IRewardsService;
import tourGuide.service.IUserService;
import tourGuide.service.TourGuideService;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
@RunWith(SpringRunner.class)
public class RewardsServiceIT {

	@Autowired
	private IRewardsService rewardsService;
	@Autowired
	private TourGuideService tourGuideService;
	@Autowired
	private IUserService userService;

	private final GpsUtil gpsUtil = new GpsUtil();

	@Test
	public void userGetRewards() {
		InternalTestHelper.setInternalUserNumber(0);
		User user = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");

		Attraction attraction = gpsUtil.getAttractions().get(0);
		user.addToVisitedLocations(new VisitedLocation(user.getUserId(), attraction, new Date()));
		tourGuideService.trackUserLocation(user);
		List<UserReward> userRewards = user.getUserRewards();
		tourGuideService.tracker.stopTracking();
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
		List<UserReward> userRewards = tourGuideService.getUserRewards(userService.getAllUsers().get(0));
		tourGuideService.tracker.stopTracking();

		assertEquals(gpsUtil.getAttractions().size(), userRewards.size());
	}
	
}
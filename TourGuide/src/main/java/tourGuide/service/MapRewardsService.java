package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rewardCentral.RewardCentral;
import tourGuide.constants.TourGuideConstants;
import tourGuide.model.User;
import tourGuide.model.UserReward;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@Slf4j
public class MapRewardsService implements RewardsService {

	// proximity in miles
	private       int           proximityBuffer = TourGuideConstants.DEFAULT_PROXIMITY_BUFFER;
	private final GpsUtil       gpsUtil;
	private final RewardCentral rewardsCentral;
	ExecutorService executorService = Executors.newFixedThreadPool(100);

	public MapRewardsService(GpsUtil gpsUtil, RewardCentral rewardCentral) {
		this.gpsUtil        = gpsUtil;
		this.rewardsCentral = rewardCentral;
	}

	public void setProximityBuffer(int proximityBuffer) {
		this.proximityBuffer = proximityBuffer;
	}

	public void setDefaultProximityBuffer() {
		proximityBuffer = TourGuideConstants.DEFAULT_PROXIMITY_BUFFER;
	}

	public void calculateRewards(User user) {
		List<VisitedLocation> userLocations = user.getVisitedLocations();
		List<Attraction>      attractions   = gpsUtil.getAttractions();

		CompletableFuture.runAsync(() -> {
			for (VisitedLocation visitedLocation : userLocations) {
				for (Attraction attraction : attractions) {
					if (nearAttraction(visitedLocation, attraction)) {
						// and is currently near this attraction, add the reward for this attraction to this user
						log.debug(
								"Adding a reward for user {} for visiting attraction {} ({}:{}) because they were located at ({}:{})",
								user.getUserName(), attraction.attractionName, attraction.longitude,
								attraction.latitude,
								visitedLocation.location.longitude, visitedLocation.location.latitude);
						user.addUserReward(
								new UserReward(visitedLocation, attraction, getRewardPoints(attraction, user)));
					}
				}
			}
		}, executorService);
	}

	public boolean isWithinAttractionProximity(Attraction attraction, Location location) {
		return (getDistance(attraction, location) <= TourGuideConstants.ATTRACTION_PROXIMITY_RANGE);
	}

	private boolean nearAttraction(VisitedLocation visitedLocation, Attraction attraction) {
		return !(getDistance(attraction, visitedLocation.location) > proximityBuffer);
	}

	public int getRewardPoints(Attraction attraction, User user) {
		return rewardsCentral.getAttractionRewardPoints(attraction.attractionId, user.getUserId());
	}

	public double getDistance(Location loc1, Location loc2) {
		double lat1 = Math.toRadians(loc1.latitude);
		double lon1 = Math.toRadians(loc1.longitude);
		double lat2 = Math.toRadians(loc2.latitude);
		double lon2 = Math.toRadians(loc2.longitude);

		double angle = Math.acos(Math.sin(lat1) * Math.sin(lat2)
				+ Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));

		double nauticalMiles = 60 * Math.toDegrees(angle);
		return TourGuideConstants.STATUTE_MILES_PER_NAUTICAL_MILE * nauticalMiles;
	}
}
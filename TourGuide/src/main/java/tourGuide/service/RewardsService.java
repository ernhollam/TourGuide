package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import tourGuide.model.User;

import java.util.concurrent.ExecutionException;

/**
 * Everything related to reward calculation is defined in this interface.
 */
public interface RewardsService {
	/**
	 * Sets the maximal distance between a location and an attraction.
	 *
	 * @param proximityBuffer maximal distance
	 */
	void setProximityBuffer(int proximityBuffer);

	/**
	 * Sets default maximal distance to be used when finding if two locations are close enough each other.
	 */
	void setDefaultProximityBuffer();

	/**
	 * Adds rewards for each attraction a user visited.
	 *
	 * @param user user for whom the rewards are to be calculated
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	void calculateRewards(User user) throws ExecutionException, InterruptedException;

	/**
	 * Returns true if a user's location is within proximity buffer with attraction's location.
	 *
	 * @param attraction location of attraction
	 * @param location   user's visited location
	 *
	 * @return true if the distance between the user's visited location and the attraction is less than proximity buffer
	 */
	boolean isWithinAttractionProximity(Attraction attraction, Location location);

	/**
	 * Returns the number of points to which a user is entitled for visiting a tourist attraction
	 *
	 * @param attraction tourist attraction visited by user
	 * @param user       user for whom the points are to be calculated
	 *
	 * @return number of points to which the user is entitled
	 */
	int getRewardPoints(Attraction attraction, User user);

	/**
	 * Returns distance, in miles, between two locations
	 *
	 * @param loc1 first location
	 * @param loc2 second location
	 *
	 * @return distance in miles
	 */
	double getDistance(Location loc1, Location loc2);
}
package tourGuide.service;

import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.ViewModel.NearbyAttractionViewModel;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * Implements TourGuide's basic features.
 */
public interface TourGuideService {

	/**
	 * Returns the list of rewards a user received for each tourist attraction they visited.
	 *
	 * @param user user for which the rewards have to be returned
	 *
	 * @return a UserReward object which contains information regarding the visited location, the tourist attraction and
	 * the corresponding points
	 *
	 * @see UserReward
	 * @see VisitedLocation
	 * @see gpsUtil.location.Attraction
	 */
	List<UserReward> getUserRewards(User user);

	/**
	 * Tracks a user if location history is empty, or returns their last visited location.
	 *
	 * @param user user to be tracked.
	 *
	 * @return VisitedLocation object which contains their ID, the location and the time they visited the location
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see VisitedLocation
	 * @see Location
	 */
	VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException;

	/**
	 * Returns a list of providers which is equivalent to list of trip recommendations for user according to their
	 * travel preferences.
	 *
	 * @param user user to find trip recommendations for
	 *
	 * @return a list of providers corresponding to user's preferences
	 *
	 * @see Provider
	 * @see tourGuide.model.UserPreferences
	 */
	List<Provider> getTripDeals(User user);

	/**
	 * Returns the location of the mobile phone or the user's laptop.
	 *
	 * The method uses asynchronous CompletableFuture to track a user in a separate thread, then updates the user by
	 * updating their location history and adding the reward points corresponding to their visited locations in another
	 * thread when we call the get() method.
	 *
	 * @param user user to be located
	 *
	 * @return a VisitedLocation encapsulated in a CompletableFuture allowing asynchronous behaviour
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see CompletableFuture
	 * @see VisitedLocation
	 */
	CompletableFuture<VisitedLocation> trackUserLocation(User user) throws ExecutionException, InterruptedException;

	/**
	 * Get the closest tourist attractions to the user - no matter how far away they are.
	 *
	 * @param user user for which the closest tourist attractions are to be found
	 *
	 * @return a list of objects that contain: Name of Tourist attraction, Tourist attractions lat/long, The user's
	 * location lat/long, The distance in miles between the user's location and each of the attractions. The reward
	 * points for visiting each Attraction.
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 * @see NearbyAttractionViewModel
	 */
	List<NearbyAttractionViewModel> getNearByAttractions(User user) throws ExecutionException, InterruptedException;

	/**
	 * Returns locations of all users currently using the app, found by consulting their stored location history. For
	 * performances reasons, gpsUtil is not used.
	 *
	 * @return Map with user and their most recent location
	 *
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	Map<UUID, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException;
}
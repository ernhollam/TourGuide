package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.javamoney.moneta.Money;
import org.springframework.stereotype.Service;
import tourGuide.constants.TourGuideConstants;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.ViewModel.NearbyAttractionViewModel;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MapTourGuideService implements TourGuideService {
	private final GpsUtil    gpsUtil;
	private final TripPricer tripPricer = new TripPricer();

	private final RewardsService  rewardsService;
	private final UserService     userService;
	private final ExecutorService executorService = Executors.newFixedThreadPool(100);

	public MapTourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
		this.gpsUtil        = gpsUtil;
		this.rewardsService = rewardsService;
		this.userService    = userService;
	}

	/**
	 * Returns user rewards.
	 *
	 * @param user user to find rewards for
	 * @return list of user's rewards
	 */
	public List<UserReward> getUserRewards(User user) {
		return user.getUserRewards();
	}

	/**
	 * Returns current location for a user
	 *
	 * @param user user for which the location is wanted
	 * @return user's last visited location or current location
	 */
	public VisitedLocation getUserLocation(User user) throws ExecutionException, InterruptedException {
		return (user.getVisitedLocations().isEmpty()) ?
				trackUserLocation(user).get() : user.getLastVisitedLocation();
	}

	/**
	 * Gets trip recommandations for user.
	 *
	 * @param user ser to find recommandations for
	 * @return a list of providers corresponding to user's preferences
	 */
	public List<Provider> getTripDeals(User user) {
		int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
		// get list of providers based on user preferences regarding nb of adults, nb of children and trip duration
		List<Provider> providers = tripPricer.getPrice(TourGuideConstants.TRIP_PRICER_API_KEY, user.getUserId(),
				user.getUserPreferences().getNumberOfAdults(),
				user.getUserPreferences().getNumberOfChildren(),
				user.getUserPreferences().getTripDuration(),
				cumulativeRewardPoints);
		// find trip deals within user's price range
		Money userLowerPricePoint  = user.getUserPreferences().getLowerPricePoint();
		Money userHigherPricePoint = user.getUserPreferences().getHighPricePoint();
		List<Provider> providersWithinUsersPriceRange = providers
				.stream()
				.filter(provider -> Money.of(provider.price, user.getUserPreferences().getCurrency())
						.isGreaterThanOrEqualTo(userLowerPricePoint)
						&& Money.of(provider.price, user.getUserPreferences().getCurrency())
						.isLessThanOrEqualTo(userHigherPricePoint))
				.collect(Collectors.toList());
		user.setTripDeals(providersWithinUsersPriceRange);
		return providersWithinUsersPriceRange;
	}

	public CompletableFuture<VisitedLocation> trackUserLocation(User user) {
		log.debug("Tracking user " + user.getUserName());
		return CompletableFuture
				.supplyAsync(() -> gpsUtil.getUserLocation(user.getUserId()), executorService)
				.thenApply(visitedLocation -> {
					log.debug("Updating user " + user.getUserName());
					try {
						user.addToVisitedLocations(visitedLocation);
						rewardsService.calculateRewards(user);
					} catch (ExecutionException | InterruptedException e) {
						log.error("An error occurred while adding reward to user {}", user.getUserName(), e);
					}
					return visitedLocation;
				});
	}

	/**
	 * Get the closest tourist attractions to the user - no matter how far away they are.
	 *
	 * @return a list of objects that contain: Name of Tourist attraction, Tourist attractions lat/long, The user's
	 * location lat/long, The distance in miles between the user's location and each of the attractions. The reward
	 * points for visiting each Attraction.
	 */
	public List<NearbyAttractionViewModel> getNearByAttractions(User user)
			throws ExecutionException, InterruptedException {
		VisitedLocation lastVisitedLocation = getUserLocation(user);
		List<Attraction> closestAttractions = gpsUtil.getAttractions()
				.stream()
				.sorted(Comparator.comparingDouble(
						attraction -> rewardsService.getDistance(lastVisitedLocation.location, attraction)))
				.limit(TourGuideConstants.NUMBER_OF_NEARBY_ATTRACTIONS)
				.collect(Collectors.toList());

		List<NearbyAttractionViewModel> nearbyAttractions = new CopyOnWriteArrayList<>();
		for (Attraction attraction : closestAttractions) {
			nearbyAttractions.add(new NearbyAttractionViewModel(attraction.attractionName,
					attraction,
					lastVisitedLocation.location,
					rewardsService.getDistance(lastVisitedLocation.location, attraction),
					rewardsService.getRewardPoints(attraction, user)));
		}
		return nearbyAttractions;
	}

	/**
	 * Returns locations of all users currently using the app
	 *
	 * @return Map with user and their location
	 */
	public Map<UUID, Location> getAllCurrentLocations() throws ExecutionException, InterruptedException {
		List<User>          users            = userService.getAllUsers();
		Map<UUID, Location> currentLocations = new ConcurrentHashMap<>();
		for (User user : users) {
			currentLocations.put(user.getUserId(), getUserLocation(user).location);
		}
		return currentLocations;
	}
}
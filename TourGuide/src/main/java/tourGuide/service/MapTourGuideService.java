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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MapTourGuideService implements TourGuideService {
    private final GpsUtil    gpsUtil;
    private final TripPricer tripPricer = new TripPricer();

    private final RewardsService rewardsService;
    private final UserService    userService;


    public MapTourGuideService(GpsUtil gpsUtil, RewardsService rewardsService, UserService userService) {
        this.gpsUtil        = gpsUtil;
        this.rewardsService = rewardsService;
        this.userService    = userService;
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().isEmpty()) ? trackUserLocation(user) : user.getLastVisitedLocation();
    }


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
                .filter(provider -> Money.of(provider.price, user.getUserPreferences().getCurrency()).isGreaterThanOrEqualTo(userLowerPricePoint)
                        && Money.of(provider.price, user.getUserPreferences().getCurrency()).isLessThanOrEqualTo(userHigherPricePoint))
                .collect(Collectors.toList());
        user.setTripDeals(providersWithinUsersPriceRange);
        return providersWithinUsersPriceRange;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<Attraction> getAttractionsWithinProximityRange(VisitedLocation visitedLocation) {
        List<Attraction> nearbyAttractions = new ArrayList<>();
        for (Attraction attraction : gpsUtil.getAttractions()) {
            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }
        return nearbyAttractions;
    }

    public List<NearbyAttractionViewModel> getNearByAttractions(User user) {
        VisitedLocation lastVisitedLocation = getUserLocation(user);
        List<Attraction> closestAttractions = gpsUtil.getAttractions()
                .stream()
                .sorted(Comparator.comparingDouble(attraction -> rewardsService.getDistance(lastVisitedLocation.location, attraction)))
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

    public Map<User, Location> getAllCurrentLocations() {
        List<User>          users            = userService.getAllUsers();
        Map<User, Location> currentLocations = new ConcurrentHashMap<>();
        for (User user : users) {
            currentLocations.put(user, getUserLocation(user).location);
        }
        return currentLocations;
    }
}
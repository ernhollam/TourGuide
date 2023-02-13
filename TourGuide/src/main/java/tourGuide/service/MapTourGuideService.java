package tourGuide.service;

import gpsUtil.GpsUtil;
import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourGuide.constants.TourGuideConstants;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tripPricer.Provider;
import tripPricer.TripPricer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
@Slf4j
public class MapTourGuideService implements TourGuideService {
    private final GpsUtil    gpsUtil;
    private final TripPricer tripPricer = new TripPricer();

    private final RewardsService rewardsService;


    public MapTourGuideService(GpsUtil gpsUtil, RewardsService rewardsService) {
        this.gpsUtil        = gpsUtil;
        this.rewardsService = rewardsService;
    }

    public List<UserReward> getUserRewards(User user) {
        return user.getUserRewards();
    }

    public VisitedLocation getUserLocation(User user) {
        return (user.getVisitedLocations().size() > 0) ?
               user.getLastVisitedLocation() :
               trackUserLocation(user);
    }


    public List<Provider> getTripDeals(User user) {
        int cumulativeRewardPoints = user.getUserRewards().stream().mapToInt(UserReward::getRewardPoints).sum();
        List<Provider> providers = tripPricer.getPrice(TourGuideConstants.TRIP_PRICER_API_KEY, user.getUserId(), user.getUserPreferences().getNumberOfAdults(),
                user.getUserPreferences().getNumberOfChildren(), user.getUserPreferences().getTripDuration(), cumulativeRewardPoints);
        user.setTripDeals(providers);
        return providers;
    }

    public VisitedLocation trackUserLocation(User user) {
        VisitedLocation visitedLocation = gpsUtil.getUserLocation(user.getUserId());
        user.addToVisitedLocations(visitedLocation);
        rewardsService.calculateRewards(user);
        return visitedLocation;
    }

    public List<Attraction> getNearByAttractions(VisitedLocation visitedLocation) {
        List<Attraction> nearbyAttractions = new ArrayList<>();
        for (Attraction attraction : gpsUtil.getAttractions()) {
            if (rewardsService.isWithinAttractionProximity(attraction, visitedLocation.location)) {
                nearbyAttractions.add(attraction);
            }
        }

        return nearbyAttractions;
    }

    public Map<String, Map<String, String>> getFiveClosestAttractions(User user) {
        VisitedLocation     lastVisitedLocation = getUserLocation(user);
        Map<String, Double> distanceToUser      = new HashMap<>();
        for (Attraction attraction : gpsUtil.getAttractions()) {
            distanceToUser.put(attraction.attractionName, rewardsService.getDistance(lastVisitedLocation.location, attraction));
        }
        Stream<Map.Entry<String, Double>> sortedListOfAttractionsByDistance =
                distanceToUser
                        .entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByValue())
                        .limit(5);
        //TODO remettre les infos demandées dans la map et retourner la bonne Map
        return null;
    }




}
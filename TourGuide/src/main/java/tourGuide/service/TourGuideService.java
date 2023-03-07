package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tourGuide.model.ViewModel.NearbyAttractionViewModel;
import tripPricer.Provider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface TourGuideService {

    List<UserReward> getUserRewards(User user);
    VisitedLocation getUserLocation(User user);
    List<Provider> getTripDeals(User user);
    VisitedLocation trackUserLocation(User user);
    List<Attraction> getAttractionsWithinProximityRange(VisitedLocation visitedLocation);
    List<NearbyAttractionViewModel> getNearByAttractions(User user);
    Map<UUID, Location> getAllCurrentLocations();
}
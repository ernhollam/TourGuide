package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.VisitedLocation;
import tourGuide.model.User;
import tourGuide.model.UserReward;
import tripPricer.Provider;

import java.util.List;

public interface TourGuideService {

    List<UserReward> getUserRewards(User user);
    VisitedLocation getUserLocation(User user);
    List<Provider> getTripDeals(User user);
    VisitedLocation trackUserLocation(User user);
    List<Attraction> getNearByAttractions(VisitedLocation visitedLocation);
}
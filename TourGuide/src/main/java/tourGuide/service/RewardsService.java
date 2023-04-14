package tourGuide.service;

import gpsUtil.location.Attraction;
import gpsUtil.location.Location;
import tourGuide.model.User;

import java.util.concurrent.ExecutionException;

public interface RewardsService {
    void setProximityBuffer(int proximityBuffer);
    void setDefaultProximityBuffer();
    void calculateRewards(User user) throws ExecutionException, InterruptedException;
    boolean isWithinAttractionProximity(Attraction attraction, Location location);
    int getRewardPoints(Attraction attraction, User user);
    double getDistance(Location loc1, Location loc2);
}
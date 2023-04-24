package tourGuide.model.ViewModel;

import gpsUtil.location.Location;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Object to return when asking for nearby attractions. Object must contain: - Name of tourist attraction, - Tourist
 * attraction's latitude and longitude, - The user's location latitude and longitude, - The distance in miles between
 * the user's location and each of the attractions. - The reward points for visiting the attraction.
 */
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class NearbyAttractionViewModel {
	String   attractionName;
	Location attractionLocation;
	Location userLocation;
	double   distanceInMiles;
	double   rewardPoints;
}
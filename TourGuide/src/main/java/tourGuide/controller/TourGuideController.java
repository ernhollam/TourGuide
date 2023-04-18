package tourGuide.controller;

import com.jsoniter.output.JsonStream;
import gpsUtil.location.VisitedLocation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tourGuide.model.User;
import tourGuide.service.TourGuideService;
import tourGuide.service.UserService;
import tripPricer.Provider;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class TourGuideController {

	@Autowired
	TourGuideService tourGuideService;

	@Autowired
	UserService userService;

	@RequestMapping("/")
	public String index() {
		return "Greetings from TourGuide!";
	}

	/**
	 * Returns current location for a user
	 *
	 * @param userName username of user for which the location is wanted
	 * @return user's location
	 */
	@RequestMapping("/getLocation")
	public String getLocation(@RequestParam String userName) throws ExecutionException, InterruptedException {
		VisitedLocation visitedLocation = tourGuideService.getUserLocation(getUser(userName));
		return JsonStream.serialize(visitedLocation.location);
	}

	/**
	 * Get the closest five tourist attractions to the user - no matter how far away they are.
	 *
	 * @return JSON object that contains: Name of Tourist attraction, Tourist attractions lat/long, The user's location
	 * lat/long, The distance in miles between the user's location and each of the attractions. The reward points for
	 * visiting each Attraction.
	 */

	@RequestMapping("/getNearbyAttractions")
	public String getNearbyAttractions(@RequestParam String userName) throws ExecutionException, InterruptedException {
		return JsonStream.serialize(tourGuideService.getNearByAttractions(getUser(userName)));
	}

	/**
	 * Returns user rewards.
	 *
	 * @param userName username of user to find rewards
	 * @return list of user's rewards
	 */
	@RequestMapping("/getRewards")
	public String getRewards(@RequestParam String userName) {
		return JsonStream.serialize(tourGuideService.getUserRewards(getUser(userName)));
	}

	/**
	 * Returns locations of all users currently using the app
	 *
	 * @return Map with user and their location
	 */
	@RequestMapping("/getAllCurrentLocations")
	public String getAllCurrentLocations() throws ExecutionException, InterruptedException {
		// Get a list of every user's most recent location as JSON
		//- Note: does not use gpsUtil to query for their current location,
		//        but rather gathers the user's current location from their stored location history.
		//
		// Return object should be the just a JSON mapping of userId to Locations similar to:
		//     {
		//        "019b04a9-067a-4c76-8817-ee75088c3822": {"longitude":-48.188821,"latitude":74.84371}
		//        ...
		//     }

		return JsonStream.serialize(tourGuideService.getAllCurrentLocations());
	}

	/**
	 * Gets trip recommendations for user.
	 *
	 * @param userName username of user to find recommendations for
	 * @return a list of providers corresponding to user's preferences
	 */
	@RequestMapping("/getTripDeals")
	public String getTripDeals(@RequestParam String userName) {
		List<Provider> providers = tourGuideService.getTripDeals(getUser(userName));
		return JsonStream.serialize(providers);
	}

	/**
	 * Finds user by its username.
	 *
	 * @param userName username of user to find
	 * @return a user if exists
	 */
	private User getUser(String userName) {
		return userService.getUser(userName);
	}
}
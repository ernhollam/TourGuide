package tourGuide.tracker;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tourGuide.constants.TourGuideConstants;
import tourGuide.model.User;
import tourGuide.service.ITourGuideService;
import tourGuide.service.IUserService;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class Tracker extends Thread {

	private final ExecutorService executorService = Executors.newSingleThreadExecutor();
	@Autowired
	private final ITourGuideService tourGuideService;
	@Autowired final IUserService userService;
	private boolean stop = false;

	public Tracker(ITourGuideService tourGuideService, IUserService userService) {
		this.tourGuideService = tourGuideService;
		this.userService = userService;
		executorService.submit(this);
	}
	
	/**
	 * Assures to shut down the Tracker thread
	 */
	public void stopTracking() {
		stop = true;
		executorService.shutdownNow();
	}
	
	@Override
	public void run() {
		StopWatch stopWatch = new StopWatch();
		while(true) {
			if(Thread.currentThread().isInterrupted() || stop) {
				log.debug("Tracker stopping");
				break;
			}
			
			List<User> users = userService.getAllUsers();
			log.debug("Begin Tracker. Tracking " + users.size() + " users.");
			stopWatch.start();
			users.forEach(tourGuideService::trackUserLocation);
			stopWatch.stop();
			log.debug("Tracker Time Elapsed: " + TimeUnit.MILLISECONDS.toSeconds(stopWatch.getTime()) + " seconds."); 
			stopWatch.reset();
			try {
				log.debug("Tracker sleeping");
				TimeUnit.SECONDS.sleep(TourGuideConstants.TRACKING_POLLING_INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
		}
		
	}
}
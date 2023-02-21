package tourGuide.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tourGuide.config.TestModeConfiguration;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.util.LocationUtil;

import java.util.*;
import java.util.stream.IntStream;

@Service
@Slf4j
public class MapUserService implements UserService {
    // Database connection will be used for external users, but for testing purposes internal users are provided and stored in memory
    private final Map<String, User> internalUserMap = new HashMap<>();

    public MapUserService(TestModeConfiguration testModeConfiguration) {
        if (testModeConfiguration.isTestMode()) {
            log.info("TestMode enabled");
            log.debug("Initializing users");
            initializeInternalUsers();
            log.debug("Finished initializing users");
        }
    }
    public User getUser(String userName) {
        return internalUserMap.get(userName);
    }

    public List<User> getAllUsers() {
        return new ArrayList<>(internalUserMap.values());
    }

    public void addUser(User user) {
        if (!internalUserMap.containsKey(user.getUserName())) {
            internalUserMap.put(user.getUserName(), user);
        }
    }

    public void initializeInternalUsers() {
        IntStream.range(0, InternalTestHelper.getInternalUserNumber()).forEach(i -> {
            String userName = "internalUser" + i;
            String phone    = "000";
            String email    = userName + "@tourGuide.com";
            User   user     = new User(UUID.randomUUID(), userName, phone, email);
            LocationUtil.generateUserLocationHistory(user);

            internalUserMap.put(userName, user);
        });
        log.debug("Created " + InternalTestHelper.getInternalUserNumber() + " internal test users.");
    }


}
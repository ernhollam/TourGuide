package tourGuide.integration;

import gpsUtil.GpsUtil;
import org.junit.Test;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.UserService;
import tourGuide.service.MapRewardsService;
import tourGuide.service.MapUserService;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@Import(MapUserService.class)
public class MapUserServiceIT {
    @Autowired
    UserService       userService;
    @Autowired
    MapRewardsService mapRewardsService;
    GpsUtil gpsUtil = new GpsUtil();
    User user;
    User user2;

    @BeforeEach
    public void setUp() {
        InternalTestHelper.setInternalUserNumber(0);
    }

    @BeforeAll
    public void initializeUsers() {
        user  = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");

        userService.addUser(user);
        userService.addUser(user2);
    }
    @Test
    public void getAllUsers() {

        List<User> allUsers = userService.getAllUsers();

        // userService.tracker.stopTracking();

        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    public void addUser() {
        User retrievedUser  = userService.getUser(user.getUserName());
        User retrievedUser2 = userService.getUser(user2.getUserName());

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
    }
}
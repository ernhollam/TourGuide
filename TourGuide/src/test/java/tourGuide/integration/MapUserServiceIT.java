package tourGuide.integration;

import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import tourGuide.config.TestModeConfiguration;
import tourGuide.helper.InternalTestHelper;
import tourGuide.model.User;
import tourGuide.service.MapUserService;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(SpringExtension.class)
@SpringBootTest
public class MapUserServiceIT {

    MapUserService userService;
    private final TestModeConfiguration testModeConfiguration = new TestModeConfiguration();
    private User user;
    private User user2;

    @Before
    public void setUp(){
        // reset internal user number
        InternalTestHelper.setInternalUserNumber(0);
        userService = new MapUserService(testModeConfiguration);

        // GIVEN two users added
        user  = new User(UUID.randomUUID(), "jon", "000", "jon@tourGuide.com");
        user2 = new User(UUID.randomUUID(), "jon2", "000", "jon2@tourGuide.com");
        userService.addUser(user);
        userService.addUser(user2);
    }
    @Test
    @DisplayName("getAllUsers() should return all existing users")
    public void getAllUsers() {
        //WHEN
        List<User> allUsers = userService.getAllUsers();
        //THEN getAllUsers() should return the two users added previously
        assertTrue(allUsers.contains(user));
        assertTrue(allUsers.contains(user2));
    }

    @Test
    @DisplayName("getUser should return the right user")
    public void getUser() {
        User retrievedUser  = userService.getUser(user.getUserName());
        User retrievedUser2 = userService.getUser(user2.getUserName());

        assertEquals(user, retrievedUser);
        assertEquals(user2, retrievedUser2);
    }
}
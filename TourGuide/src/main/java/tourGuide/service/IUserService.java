package tourGuide.service;

import tourGuide.model.User;

import java.util.List;

public interface IUserService {

    /**
     * Gets user with username.
     * @param userName username of user to find
     * @return a user if exists
     */
    User getUser(String userName);

    /**
     * Returns a list of all users.
     * @return list of all users.
     */
    List<User> getAllUsers();

    /**
     * Adds new user to the app.
     * @param user user to add
     */
    void addUser(User user);

    /**
     * Creates test users.
     */
    void initializeInternalUsers();
}
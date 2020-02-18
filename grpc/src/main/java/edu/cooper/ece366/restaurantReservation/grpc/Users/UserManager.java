package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import org.jdbi.v3.core.Jdbi;

public class UserManager {

    private Jdbi db;

    public UserManager(Jdbi db){
        this.db = db;
    }

    public int checkAndInsertUser(User user) throws InvalidUsernameException, InvalidNameException,
            ContactManager.InvalidContactIdException, ContactManager.InvalidPhoneException, ContactManager.InvalidEmailException {
        checkUsername(user.getUsername());
        checkName(user.getFname(),"First");
        checkName(user.getLname(),"Last");
        ContactManager cm = new ContactManager(db);
        int contId = cm.checkAndInsertContact(user.getContact());

        return contId;
    }

    public void checkUsername(String username) throws InvalidUsernameException {
        if (!username.matches("^[a-zA-Z0-9]*$")) {
            throw new InvalidUsernameException("Username must only include alphanumeric characters.");
        }

        if(db.withExtension(UserDao.class, d -> d.userExists(username))){
            throw new InvalidUsernameException("Username exists");
        }
    }

    public void checkName(String name, String type) throws InvalidNameException {
        if (!name.matches("^[a-zA-Z]*$")) {
            throw new InvalidNameException(type + " name must only include alphabet characters.");
        }
    }

    public User getUser(int id) {
        User reply = db.withExtension(UserDao.class, dao -> {
            return dao.getUser(id);
        });

        return reply;
    }

    public static class InvalidUsernameException extends Exception {
        public InvalidUsernameException(String message) {
            super(message);
        }
    }

    public static class InvalidNameException extends Exception {
        public InvalidNameException(String message) {
            super(message);
        }
    }
}

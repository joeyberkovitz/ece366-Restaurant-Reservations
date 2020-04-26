package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class UserManager {

    private Jdbi db;

    public UserManager(Jdbi db){
        this.db = db;
    }

    public User setUser(User user)
            throws  NoIdException,
            InvalidUsernameException,
            InvalidNameException,
            ContactManager.InvalidContactIdException,
            ContactManager.InvalidPhoneException,
            ContactManager.InvalidEmailException {
        ContactManager cm = new ContactManager(db);

        if (user.getId() == 0)
            throw new NoIdException("User id not provided.");

        if (!existsUser(user.getUsername()))
            throw new InvalidUsernameException("Username does not exist.");

        checkName(user.getFname(),"First");
        checkName(user.getLname(),"Last");

        cm.setContact(user.getContact());

        db.withExtension(UserDao.class, dao -> {
            dao.setUser(user);
            return null;
        });

        return db.withExtension(UserDao.class, dao -> {
            return dao.getUser(user.getId());
        });
    }

    public boolean existsUser(String username) {
        return db.withExtension(UserDao.class, dao -> dao.userExists(username));
    }

    public void checkUsername(String username) throws InvalidUsernameException {
        if (!username.matches("(\\p{IsLatin}|[0-9 ])*$")) {
            throw new InvalidUsernameException("Username must only include alphanumeric characters.");
        }

        if(db.withExtension(UserDao.class, dao -> dao.userExists(username))){
            throw new InvalidUsernameException("Username exists");
        }
    }

    public void checkName(String name, String type) throws InvalidNameException {
        if (!name.matches("^[a-zA-Z]*$")) {
            throw new InvalidNameException(type + " name must only include alphabet characters.");
        }
    }

    public User getUser(int id) {
        return db.withExtension(UserDao.class, dao -> {
            return dao.getUser(id);
        });
    }
    
    public int getIdByUsername(String username) throws InvalidUsernameException {
        Optional<Integer> userId = db.withExtension(UserDao.class, dao -> {
            return dao.getIdByUsername(username);
        });
        if (userId.isPresent())
            return userId.get();
        else
            throw new InvalidUsernameException("Username does not exist.");
    }

    public static class NoIdException extends Exception {
        public NoIdException(String message) { super(message); }
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

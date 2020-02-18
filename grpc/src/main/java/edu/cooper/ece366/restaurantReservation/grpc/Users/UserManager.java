package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import org.jdbi.v3.core.Jdbi;

public class UserManager {

    private Jdbi db;

    public UserManager(Jdbi db){
        this.db = db;
    }

    public User setUser(User user) throws InvalidNameException, ContactManager.InvalidContactIdException, ContactManager.InvalidPhoneException, ContactManager.InvalidEmailException, InvalidUserException {
        ContactManager cm = new ContactManager(db);

        if (user.getId() == 0) {
            //todo throw error
        }
        else {
            //todo check by username or id?
            if (!existsUser(user.getUsername()))
                throw new InvalidUserException("User does not exits.");

            if (!user.getFname().isEmpty())
                checkName(user.getFname(),"First");
            if (!user.getLname().isEmpty())
                checkName(user.getLname(),"Last");

            // todo check if contact is completely empty
            Contact newContact = cm.setContact(user.getContact());

            db.withExtension(UserDao.class, dao -> {
                dao.setUser(user, newContact.getId());
                return null;
            });
        }

        return db.withExtension(UserDao.class, dao -> {
            return dao.getUser(user.getId());
        });
    }

    public boolean existsUser(String username) {
        return db.withExtension(UserDao.class, dao -> dao.userExists(username));
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

    public static class InvalidUserException extends Exception {
        public InvalidUserException(String message) {
            super(message);
        }
    }
}

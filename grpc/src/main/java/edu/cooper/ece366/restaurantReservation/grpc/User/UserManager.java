package edu.cooper.ece366.restaurantReservation.grpc.User;

import edu.cooper.ece366.restaurantReservation.grpc.Contact.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.RestaurantServiceOuterClass;
import org.jdbi.v3.core.Jdbi;

public class UserManager {

    private Jdbi db;

    public UserManager(Jdbi db){
        this.db = db;
    }

    public int checkUser(RestaurantServiceOuterClass.User user) throws InvalidUsernameException, InvalidNameException, ContactManager.InvalidContactIdException, ContactManager.InvalidPhoneException, ContactManager.InvalidEmailException {
        checkUsername(user.getUsername());
        checkName(user.getFname(),"First");
        checkName(user.getLname(),"Last");
        int contId = checkUserContact(user.getContact());

        return contId;
    }

    public void checkUsername(String username) throws InvalidUsernameException {
        if (!username.matches("^[a-zA-Z0-9]*$")) {
            throw new InvalidUsernameException("Username must only include alphanumeric characters.");
        }
    }

    public void checkName(String name, String type) throws InvalidNameException {
        if (!name.matches("^[a-zA-Z]*$")) {
            throw new InvalidNameException(type + " name must only include alphabet characters.");
        }
    }

    /* Todo we need an instance of contact manager for use here,
    however we cant instantiate it because that would imply we would have to instantiate one of every kind of
    manager within each manager all with the same "db".
    The other approach, instantiating them from main, and passing them all to constructors is impossible because
    we would have to instantiate them at the same time.
    We cant make them all static because we cant hardcode a jdbi object.
    The best solution seems to be instantiating a new manager here and then deleting it after the function call.
    However, this needs to be thought through with regards to the number of total instances.
     */
    public int checkUserContact(RestaurantServiceOuterClass.Contact contact) throws ContactManager.InvalidContactIdException, ContactManager.InvalidPhoneException, ContactManager.InvalidEmailException {
        ContactManager cm = new ContactManager(db);
        return cm.checkContact(contact);
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

package edu.cooper.ece366.restaurantReservation.grpc.Contacts;

import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class ContactManager {

    private Jdbi db;

    public ContactManager(Jdbi db){
        this.db = db;
    }

    // todo setContact

    public int checkContact(Contact contact) throws InvalidPhoneException, InvalidEmailException, InvalidContactIdException {
        int contId;
        if (contact.getId() == 0) {
            // throw exceptions
            checkPhone(contact.getPhone());
            checkEmail(contact.getEmail());

            contId = db.withExtension(ContactDao.class, dao -> {
                return dao.insertContact(contact);
            });
        }
        else {
            // Check if id exists
            Optional<Integer> valContId = db.withExtension(ContactDao.class, dao -> {
                return dao.checkContact(contact.getId());
            });
            if (!valContId.isPresent()) {
                throw new InvalidContactIdException("Contact Id does not exist.");
            }
            contId = contact.getId();
        }

        return contId;
    }

    private void checkPhone(int phone) throws InvalidPhoneException{
        if (phone == 0) {
            throw new InvalidPhoneException("Phone number field must be filled.");
        }
    }

    private void checkEmail(String email) throws InvalidEmailException {
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException("Valid email must be provided.");
        }
    }

    public static class InvalidPhoneException extends Exception {
        public InvalidPhoneException(String message) {
            super(message);
        }
    }

    public static class InvalidEmailException extends Exception {
        public InvalidEmailException(String message) {
            super(message);
        }
    }

    public static class InvalidContactIdException extends Exception {
        public InvalidContactIdException(String message) {
            super(message);
        }
    }
}

package edu.cooper.ece366.restaurantReservation.grpc.Contacts;

import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class ContactManager {

    private Jdbi db;

    public ContactManager(Jdbi db){
        this.db = db;
    }

    public int checkAndInsertContact(Contact contact)
            throws InvalidPhoneException,
            InvalidEmailException {
        int contId;
        // throw exceptions
        // todo further limit phone
        checkPhone(contact.getPhone());
        checkEmail(contact.getEmail());

        contId = db.withExtension(ContactDao.class, dao -> {
            return dao.insertContact(contact);
        });

        return contId;
    }

    public void setContact(Contact contact)
            throws InvalidPhoneException,
            InvalidEmailException,
            InvalidContactIdException {
        int contId = contact.getId();

        if (contId == 0 || nonexistContact(contId)) {
            throw new InvalidContactIdException("Contact ID does not exist.");
        }

        //todo remove nonnumeric
        checkPhone(contact.getPhone());
        checkEmail(contact.getEmail());

        db.withExtension(ContactDao.class, dao -> {
            dao.setContact(contact);
            return null;
        });
    }

    private boolean nonexistContact(int id) {
        Optional<Integer> valContId = db.withExtension(ContactDao.class, dao -> {
            return dao.checkContact(id);
        });
        return valContId.isEmpty();
    }

    private void checkPhone(String phone) throws InvalidPhoneException{
        if (phone.isBlank() || phone.matches("[a-zA-Z]")) {
            throw new InvalidPhoneException("Phone number field must be filled.");
        }
    }

    private void checkEmail(String email) throws InvalidEmailException {
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException("Valid email must be provided.");
        }
    }

    public void deleteContact(int id) throws InvalidContactIdException {
        if (nonexistContact(id))
            throw new InvalidContactIdException("Can't find contact ID");

        db.withExtension(ContactDao.class, dao -> {
            dao.deleteContact(id);
            return null;
        });
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

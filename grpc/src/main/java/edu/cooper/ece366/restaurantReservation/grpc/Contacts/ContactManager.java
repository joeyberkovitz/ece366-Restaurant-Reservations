package edu.cooper.ece366.restaurantReservation.grpc.Contacts;

import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import org.jdbi.v3.core.Jdbi;

import java.sql.SQLException;
import java.util.Optional;

public class ContactManager {

    private Jdbi db;

    public ContactManager(Jdbi db){
        this.db = db;
    }

    public int checkAndInsertContact(Contact contact) throws InvalidPhoneException, InvalidEmailException, InvalidContactIdException {
        //todo create new contact every time
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
            if (!existsContact(contact.getId())) {
                throw new InvalidContactIdException("Contact ID does not exist.");
            }
            contId = contact.getId();
        }

        return contId;
    }

    public Contact setContact(Contact contact) throws InvalidPhoneException, InvalidEmailException, InvalidContactIdException {
        int contId;
        if(contact.getId() == 0)
            contId = checkAndInsertContact(contact);
        else {
            contId = contact.getId();
            if(!existsContact(contId)) {
                throw new InvalidContactIdException("Contact ID does not exist.");
            }
            //todo remove nonnumeric
            if(!contact.getPhone().isEmpty())
                checkPhone(contact.getPhone());
            if(!contact.getEmail().isEmpty())
                checkEmail(contact.getEmail());

            db.withExtension(ContactDao.class, dao -> {
                dao.setContact(contact);
                return null;
            });
        }
        return db.withExtension(ContactDao.class, dao -> {
            return dao.getContact(contId);
        });
    }

    private boolean existsContact(int id) {
        Optional<Integer> valContId = db.withExtension(ContactDao.class, dao -> {
            return dao.checkContact(id);
        });
        return valContId.isPresent();
    }

    private void checkPhone(String phone) throws InvalidPhoneException{
        if (phone.isBlank()) {
            throw new InvalidPhoneException("Phone number field must be filled.");
        }
    }

    private void checkEmail(String email) throws InvalidEmailException {
        if (!email.matches("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")) {
            throw new InvalidEmailException("Valid email must be provided.");
        }
    }

    public void deleteContact(int id) throws InvalidContactIdException {
        if (!existsContact(id))
            throw new InvalidContactIdException("Can't find contact ID");
        try {
            db.withExtension(ContactDao.class, dao -> {
                dao.deleteContact(id);
                return null;
            });
        }
        catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                // SQL error 1451 represents a foreign key constraint. We ignore this to not allow deletion of contact
                // if another restaurant or user is using this contact
                if (!((SQLException) e.getCause()).getSQLState().equals("1451")) throw e;
            }
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

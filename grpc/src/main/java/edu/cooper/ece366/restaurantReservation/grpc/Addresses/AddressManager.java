package edu.cooper.ece366.restaurantReservation.grpc.Addresses;

import edu.cooper.ece366.restaurantReservation.grpc.Address;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;

public class AddressManager {
    private Jdbi db;

    public AddressManager(Jdbi db) {
        this.db = db;
    }

    public int checkAndInsertAddress(Address address)
            throws InvalidAddrNameException,
            InvalidLatException,
            InvalidLongException,
            InvalidAddrFormException,
            InvalidZipException {
        int addrId;

        // throw exceptions
        validateAddr(address);

        addrId = db.withExtension(AddressDao.class, dao -> {
            return dao.insertAddress(address);
        });
        return addrId;
    }

    public void setAddress(Address address)
            throws InvalidAddressIdException,
            InvalidAddrNameException,
            InvalidLatException,
            InvalidLongException,
            InvalidAddrFormException,
            InvalidZipException {

        int addrId = address.getId();

        if (addrId == 0 || nonexistAddress(addrId)) {
            throw new InvalidAddressIdException("Address ID does not exist.");
        }

        validateAddr(address);

        db.withExtension(AddressDao.class, dao -> {
            dao.setAddress(address);
            return null;
        });
    }

    private void validateAddr(Address address)
            throws InvalidAddrNameException,
            InvalidLatException,
            InvalidLongException,
            InvalidAddrFormException,
            InvalidZipException {
        checkName(address.getName());
        // todo are we using lat long?
        //checkLat(address.getLatitude());
        //checkLong(address.getLongitude());

        checkEmpty(address.getLine1(), "Line 1");
        checkEmpty(address.getCity(), "City");
        checkEmpty(address.getState(), "State");

        checkZip(address.getZip());
    }

    public void checkName(String name) throws InvalidAddrNameException {
        if (!name.matches("(\\p{IsLatin}|[0-9 ])*$")) {
            throw new InvalidAddrNameException("Name must only include alphanumeric characters.");
        }
    }

    public void checkLat(float latitude) throws InvalidLatException {
        if (!(-90 <= latitude && latitude <= 90)) {
            throw new InvalidLatException("Invalid latitude");
        }
    }

    public void checkLong(float longitude) throws InvalidLongException {
        if (!(-180 <= longitude && longitude <= 180)) {
            throw new InvalidLongException("Invalid longitude");
        }
    }

    public void checkEmpty(String input, String type) throws InvalidAddrFormException {
        if (input.equals("")) {
            throw new InvalidAddrFormException(type + " cannot be empty");
        }
    }

    public void checkZip(String zip) throws InvalidZipException {
        if (!zip.matches("^[0-9]{5}(-[0-9]{4})?$")) {
            throw new InvalidZipException("Invalid zip");
        }
    }

    private boolean nonexistAddress(int id) {
        Optional<Integer> valaddrId = db.withExtension(AddressDao.class, dao -> {
            return dao.checkAddress(id);
        });
        return valaddrId.isEmpty();
    }

    public void deleteAddress(int id) throws InvalidAddressIdException {
        if (nonexistAddress(id))
            throw new InvalidAddressIdException("Can't find address ID");

        db.withExtension(AddressDao.class, dao -> {
            dao.deleteAddress(id);
            return null;
        });
    }

    public static class InvalidAddressIdException extends Exception {
        public InvalidAddressIdException(String message) {
            super(message);
        }
    }

    public static class InvalidAddrNameException extends Exception {
        public InvalidAddrNameException(String message) {
            super(message);
        }
    }

    public static class InvalidLatException extends Exception {
        public InvalidLatException(String message) {
            super(message);
        }
    }

    public static class InvalidLongException extends Exception {
        public InvalidLongException(String message) {
            super(message);
        }
    }

    public static class InvalidAddrFormException extends Exception {
        public InvalidAddrFormException(String message) {
            super(message);
        }
    }

    public static class InvalidZipException extends Exception {
        public InvalidZipException(String message) {
            super(message);
        }
    }
}

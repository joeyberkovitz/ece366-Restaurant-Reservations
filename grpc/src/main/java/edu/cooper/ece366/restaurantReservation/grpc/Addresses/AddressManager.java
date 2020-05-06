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
            InvalidLine1Exception,
            InvalidCityException,
            InvalidStateException,
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
            InvalidLine1Exception,
            InvalidCityException,
            InvalidStateException,
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
            InvalidLine1Exception,
            InvalidCityException,
            InvalidStateException,
            InvalidZipException {
        checkName(address.getName());
        checkLat(address.getLatitude());
        checkLong(address.getLongitude());

        checkLine1(address.getLine1());
        checkCity(address.getCity());
        checkState(address.getState());
        checkZip(address.getZip());
    }

    public void checkName(String name) throws InvalidAddrNameException {
        if (!name.matches("([a-zA-Z0-9 \\\"\\;\\:\\'\\?\\/\\<\\>\\,\\{\\}\\[\\]\\(\\)\\!\\@\\#\\$\\%\\^\\&\\*\\-\\_=+])*$")) {
            throw new InvalidAddrNameException("Invalid characters in address name");
        }
    }

    public void checkLat(double latitude) throws InvalidLatException {
        if (!(-90 <= latitude && latitude <= 90)) {
            throw new InvalidLatException("Invalid latitude");
        }
    }

    public void checkLong(double longitude) throws InvalidLongException {
        if (!(-180 <= longitude && longitude <= 180)) {
            throw new InvalidLongException("Invalid longitude");
        }
    }

    public void checkLine1(String input) throws InvalidLine1Exception {
        if (input.equals("")) {
            throw new InvalidLine1Exception("Line1 cannot be empty");
        }
    }

    public void checkCity(String input) throws InvalidCityException {
        if (input.equals("")) {
            throw new InvalidCityException("City cannot be empty");
        }
    }

    public void checkState(String input) throws InvalidStateException {
        if (input.equals("")) {
            throw new InvalidStateException("State cannot be empty");
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

    public static class InvalidLine1Exception extends Exception {
        public InvalidLine1Exception(String message) {
            super(message);
        }
    }

    public static class InvalidCityException extends Exception {
        public InvalidCityException(String message) {
            super(message);
        }
    }

    public static class InvalidStateException extends Exception {
        public InvalidStateException(String message) {
            super(message);
        }
    }

    public static class InvalidZipException extends Exception {
        public InvalidZipException(String message) {
            super(message);
        }
    }
}

package edu.cooper.ece366.restaurantReservation.grpc.Addresses;

import edu.cooper.ece366.restaurantReservation.grpc.Address;
import org.jdbi.v3.core.Jdbi;

import java.sql.SQLException;
import java.util.Optional;

public class AddressManager {
    private Jdbi db;

    public AddressManager(Jdbi db) {
        this.db = db;
    }

    // todo setAddress

    public int checkAndInsertAddress(Address address) throws InvalidAddressIdException {
        int addrId;
        if (address.getId() == 0) {
            // TODO checks?
            addrId = db.withExtension(AddressDao.class, dao -> {
                return dao.insertAddress(address);
            });
        } else {
            // Check if id exists
            if (!existsAddress(address.getId())) {
                throw new InvalidAddressIdException("Address ID does not exist.");
            }
            addrId = address.getId();
        }
        return addrId;
    }

    public Address setAddress(Address address) throws InvalidAddressIdException {
        int addrId;
        if (address.getId() == 0)
            addrId = checkAndInsertAddress(address);
        else {
            addrId = address.getId();
            //TODO: checks?
            if (!existsAddress(addrId)) {
                throw new InvalidAddressIdException("Address ID does not exist.");
            }

            db.withExtension(AddressDao.class, dao -> {
                dao.setAddress(address);
                return null;
            });
        }
        return db.withExtension(AddressDao.class, dao -> {
            return dao.getAddress(addrId);
        });
    }

    private boolean existsAddress(int id) {
        Optional<Integer> valaddrId = db.withExtension(AddressDao.class, dao -> {
            return dao.checkAddress(id);
        });
        return !valaddrId.isEmpty();
    }

    public void deleteAddress(int id) throws InvalidAddressIdException {
        if (!existsAddress(id))
            throw new InvalidAddressIdException("Can't find address ID");

        try {
            db.withExtension(AddressDao.class, dao -> {
                dao.deleteAddress(id);
                return null;
            });
        }
        catch (Exception e) {
            if (e.getCause() instanceof SQLException) {
                // SQL error 1451 represents a foreign key constraint. We ignore this to not allow deletion of address
                // if another restaurant is using this address
                if (!((SQLException) e.getCause()).getSQLState().equals("1451")) throw e;
            }
        }
    }

    public static class InvalidAddressIdException extends Exception {
        public InvalidAddressIdException(String message) {
            super(message);
        }
    }
}

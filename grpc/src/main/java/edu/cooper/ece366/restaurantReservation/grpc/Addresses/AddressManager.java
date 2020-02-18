package edu.cooper.ece366.restaurantReservation.grpc.Addresses;

import edu.cooper.ece366.restaurantReservation.grpc.Address;
import java.util.Optional;
import org.jdbi.v3.core.Jdbi;

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

    public static class InvalidAddressIdException extends Exception {
        public InvalidAddressIdException(String message) {
            super(message);
        }
    }
}

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
        int contId;
        if (address.getId() == 0) {
            // TODO checks?
            contId = db.withExtension(AddressDao.class, dao -> {
                return dao.insertAddress(address);
            });
        } else {
            // Check if id exists
            Optional<Integer> valContId =
                    db.withExtension(AddressDao.class, dao -> {
                        return dao.checkAddress(address.getId());
                    });
            if (valContId.isEmpty()) {
                throw new InvalidAddressIdException("Address ID does not exist.");
            }
            contId = address.getId();
        }
        return contId;
    }

    public static class InvalidAddressIdException extends Exception {
        public InvalidAddressIdException(String message) {
            super(message);
        }
    }
}

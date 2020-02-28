package edu.cooper.ece366.restaurantReservation.grpc.Addresses;

import edu.cooper.ece366.restaurantReservation.grpc.Address;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface AddressDao {
    @SqlUpdate("INSERT INTO " +
                "address (name, latitude, longitude, line1, line2, city," +
                         "state, zip) " +
                "VALUES (:name,:latitude,:longitude,:line1,:line2,:city," +
                        ":state,:zip)")
    @GetGeneratedKeys("id")
    int insertAddress(@BindBean Address address);

    @SqlQuery("SELECT * from address WHERE id = :id")
    Address getAddress(int id);

    @SqlQuery("SELECT id FROM address WHERE id = :id")
    Optional<Integer> checkAddress(int id);

    @SqlUpdate("UPDATE address SET name = :name," +
                                  "latitude = :latitude," +
                                  "longitude = :longitude," +
                                  "line1 = :line1,line1," +
                                  "line2 = :line2," +
                                  "city = :city," +
                                  "state = :state," +
                                  "zip = :zip" +
                                  "WHERE id = :id")
    void setAddress(@BindBean Address address);
    // TODO not set if values same

    @SqlUpdate("DELETE FROM address WHERE id = :id")
    void deleteAddress(int id);
}

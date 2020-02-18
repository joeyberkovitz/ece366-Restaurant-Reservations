package edu.cooper.ece366.restaurantReservation.grpc.Addresses;

import edu.cooper.ece366.restaurantReservation.grpc.Address;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface AddressDao {
    @SqlUpdate("INSERT INTO address(name," +
                                   "latitude,longitude," +
                                   "line1,line2," +
                                   "city,state,zip)" + " VALUES " +
                                  "(:name," +
                                   ":latitude,:longitude," +
                                   ":line1,:line2," +
                                   ":city,:state,:zip)")
    @GetGeneratedKeys("id")
    int insertAddress(@BindBean Address address);

    @SqlQuery("SELECT * from address WHERE id = :id")
    Address getAddress(int id);

    @SqlQuery("SELECT id FROM address WHERE id = :id")
    Optional<Integer> checkAddress(int id);

    @SqlUpdate("UPDATE address SET name= coalesce(:name,name)," +
                                  "latitude=coalesce(:latitude,latitude) " +
                                  "longitude=coalesce(:longitude,longitude) " +
                                  "line1=coalesce(:line1,line1) " +
                                  "line2=coalesce(:line2,line2) " +
                                  "city=coalesce(:city,city) " +
                                  "state=coalesce(:state,state) " +
                                  "zip=coalesce(:zip,zip) " +
                                  "WHERE id = :id")
    void setAddress(@BindBean Address address);
    // TODO not set if values same

}

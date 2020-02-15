package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Restaurant;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface RestaurantDao {
	@SqlUpdate("INSERT INTO restaurant(name, address, contact, category) VALUES (:name, :address_id, :contact_id :category)")
	@GetGeneratedKeys("id")
	int insertRestaurant(@Bind("address_id") int address_id, @Bind("contact_id") int contact_id, @BindBean Restaurant restaurant);
}

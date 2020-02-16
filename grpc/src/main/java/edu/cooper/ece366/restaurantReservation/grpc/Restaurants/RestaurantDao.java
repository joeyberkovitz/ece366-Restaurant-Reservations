package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Restaurant;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface RestaurantDao {
	@SqlUpdate("INSERT INTO restaurant(name, address_id, contact_id, category_id) VALUES (:restaurant.name, :address_id, :contact_id, :restaurant.category.category)")
	@GetGeneratedKeys("id")
	int insertRestaurant(int address_id, int contact_id, @BindBean("restaurant") Restaurant restaurant);


	@SqlUpdate("INSERT INTO restaurant_user(restaurant_id, user_id, role_id) VALUES(:restaurant_id, :user_id, :role_id)")
	void addRestaurantRelationship(int restaurant_id, int user_id, int role_id);
}

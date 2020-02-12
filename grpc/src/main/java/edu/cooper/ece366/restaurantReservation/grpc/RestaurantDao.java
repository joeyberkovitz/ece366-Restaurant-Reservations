package edu.cooper.ece366.restaurantReservation.grpc;

import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface RestaurantDao {
	@SqlUpdate("INSERT INTO restaurant(name) VALUES (:name)")
	@GetGeneratedKeys("id")
	int insertBean(@BindBean RestaurantServiceOuterClass.Restaurant restaurant);
}

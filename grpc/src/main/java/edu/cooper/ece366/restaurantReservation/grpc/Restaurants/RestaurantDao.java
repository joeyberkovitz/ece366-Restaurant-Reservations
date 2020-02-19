package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Address;
import edu.cooper.ece366.restaurantReservation.grpc.Category;
import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurant;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public interface RestaurantDao {
	@SqlUpdate("INSERT INTO " +
			"restaurant(name, address_id,contact_id, category_id) " +
			"VALUES(:restaurant.name,:address_id, :contact_id," +
			":restaurant.category.category)")
	@GetGeneratedKeys("id")
	int insertRestaurant(int address_id, int contact_id,
	                     @BindBean("restaurant") Restaurant restaurant);

	@SqlUpdate("INSERT INTO restaurant_user(restaurant_id, user_id, role_id) "+
			"VALUES(:restaurant_id, :user_id, :role_id)")
	void addRestaurantRelationship(int restaurant_id, int user_id, int role_id)
			throws UnableToExecuteStatementException;

	@SqlQuery("SELECT r.name FROM restaurant_user ru " +
			"INNER JOIN role r ON r.id = ru.role_id " +
			"WHERE user_id = :user_id AND restaurant_id = :restaurant_id")
	Optional<String> getRestaurantUserRole(int user_id, int restaurant_id);

	@SqlQuery("select restaurant.*,address.*,contact.* from restaurant " +
	          "inner join address on restaurant.address_id = address.id " +
	          "inner join contact on restaurant.contact_id = contact.id " +
	          "where restaurant.id=:id")
	@RegisterRowMapper(RestaurantMapper.class)
	Restaurant getRestaurant(int id);

	@SqlUpdate("update restaurant set name = :restaurant.name," +
			"address_id = coalesce(:address_id, address_id)," +
			"contact_id = coalesce(:contact_id, contact_id)," +
			"category_id= coalesce(:restaurant.category.category, category_id)"+
			"where restaurant.id = :restaurant.id")
	void setRestaurant(int address_id, int contact_id,
	                         @BindBean("restaurant") Restaurant restaurant);
	// TODO figure out why this complains when category is unset

	class RestaurantMapper implements RowMapper<Restaurant> {
		@Override
		public Restaurant map(ResultSet rs, StatementContext ctx)
		throws SQLException {
			return Restaurant.newBuilder()
				.setId(rs.getInt("restaurant.id"))
				.setName(rs.getString("restaurant.name"))
				// TODO: figure out if it is possible to
				// outsource the part of this that does address
				// and contact to ProtoBeanMapper, and share it
				// wiht other code, e.g. UserMapper
				.setAddress(Address.newBuilder()
					.setName(rs.getString("address.name"))
					.setLatitude(rs.getFloat(
					             "address.latitude"))
					.setLongitude(rs.getFloat(
					              "address.longitude"))
					.setLine1(rs.getString("address.line1"))
					.setLine2(rs.getString("address.line2"))
					.setCity(rs.getString("address.city"))
					.setState(rs.getString("address.state"))
					.setZip(rs.getString("address.zip"))
					.build())
				.setContact(Contact.newBuilder()
					.setPhone(rs.getString("contact.phone"))
					.setEmail(rs.getString("contact.email"))
					.build())
				.setCategory(Category.newBuilder()
					.setCategory(rs.getInt(
					             "restaurant.category_id"))
					.build())
				.build();
		}
	}
}


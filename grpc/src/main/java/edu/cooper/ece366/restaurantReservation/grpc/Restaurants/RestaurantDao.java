package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.*;
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
import java.util.List;
import java.util.Optional;

public interface RestaurantDao {
	@SqlUpdate("INSERT INTO " +
			"restaurant(name, address_id,contact_id, category_id, capacity_factor, reservation_time) " +
			"VALUES(:restaurant.name,:address_id, :contact_id," +
			":restaurant.category.category, :restaurant.capacity, :restaurant.rtime)")
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

	@SqlQuery("SELECT restaurant.*,address.*,contact.* FROM restaurant " +
	          "INNER JOIN address ON restaurant.address_id = address.id " +
	          "INNER JOIN contact ON restaurant.contact_id = contact.id " +
	          "WHERE restaurant.id=:id")
	@RegisterRowMapper(RestaurantMapper.class)
	Restaurant getRestaurant(int id);

	@SqlQuery("SELECT capacity_factor FROM restaurant WHERE id = :id")
	int getRestaurantCapFactor(int id);

	@SqlQuery("SELECT reservation_time FROM restaurant WHERE id = :id")
	int getRestaurantReservationTime(int id);

	@SqlUpdate("UPDATE restaurant SET name = :restaurant.name," +
			"category_id = :restaurant.category.category, "+
			"capacity_factor = :restaurant.capacity, "+
			"reservation_time = :restaurant.rtime "+
			"WHERE restaurant.id = :restaurant.id")
	void setRestaurant(@BindBean("restaurant") Restaurant restaurant);
	// TODO figure out why this complains when category is unset

	@SqlQuery("SELECT restaurant.*,address.*,contact.* FROM restaurant " +
	          "INNER JOIN address ON restaurant.address_id = address.id " +
	          "INNER JOIN contact ON restaurant.contact_id = contact.id " +
	          "WHERE restaurant.category_id=:id")
	@RegisterRowMapper(RestaurantMapper.class)
	List<Restaurant> searchByCategory(int id);

	static final String RELSQL =
	"SELECT rel.*,restaurant.*,address.*,rscon.*,user.*,uscon.*," +
	"role.name AS role FROM restaurant_user rel " +
	"INNER JOIN restaurant ON rel.restaurant_id = restaurant.id "+
	"INNER JOIN address ON restaurant.address_id = address.id " +
	"INNER JOIN contact rscon ON restaurant.contact_id = rscon.id " +
	"INNER JOIN user ON rel.user_id = user.id " +
	"INNER JOIN contact uscon ON user.contact_id = uscon.id " +
	"INNER JOIN role ON rel.role_id = role.id ";
	// TODO merge those two functions
	@SqlQuery(RELSQL + "WHERE rel.restaurant_id=:id")
	@RegisterRowMapper(RelationshipMapper.class)
	List<Relationship> getRelationshipByRestaurant(int id);

	@SqlQuery(RELSQL + "WHERE rel.user_id=:id")
	@RegisterRowMapper(RelationshipMapper.class)
	List<Relationship> getRelationshipByUser(int id);

	@SqlUpdate("DELETE FROM restaurant_user WHERE user_id = :userId AND " +
		"restaurant_id = :restaurantId")
	void deleteRelationship(int userId, int restaurantId);


	@SqlQuery("SELECT * FROM `table` WHERE label = :name AND restaurant_id = :rest")
	Optional<Table> getTableByName(String name, int rest);

	@SqlQuery("SELECT * FROM `table` WHERE id = :table_id")
	Table getTableById(int table_id);

	@SqlQuery("SELECT * FROM `table` WHERE restaurant_id = :restaurant_id")
	List<Table> getRestaurantTables(int restaurant_id);

	@SqlQuery("SELECT restaurant.*, address.*, contact.* FROM restaurant " +
		"INNER JOIN `table` t ON t.restaurant_id = restaurant.id " +
		"INNER JOIN address ON restaurant.address_id = address.id " +
		"INNER JOIN contact ON restaurant.contact_id = contact.id " +
		"WHERE t.id = :table_id")
	@RegisterRowMapper(RestaurantMapper.class)
	Optional<Restaurant> getRestaurantByTable(int table_id);

	@SqlUpdate("INSERT INTO " +
		"`table`(label, capacity, restaurant_id) " +
		"VALUES(:table.label,:table.capacity, :restaurant.id)")
	@GetGeneratedKeys("id")
	int insertTable(@BindBean("table") Table table,
	                     @BindBean("restaurant") Restaurant restaurant);

	@SqlUpdate("UPDATE `table` SET label = :tab.label," +
		" capacity = :tab.capacity WHERE id = :tab.id")
	void setTable(@BindBean("tab") Table table);

	@SqlUpdate("DELETE FROM `table` WHERE id = :table_id")
	void deleteTableById(int table_id);

	@SqlUpdate("DELETE FROM restaurant WHERE id = :id")
	void deleteRestaurant(int id);

	@SqlQuery("SELECT id AS category, name FROM category")
	List<Category> getCategories();

	//Todo: implement search for lat/long + miles
	@SqlQuery("SELECT sum(t.capacity) as 'availableCapacity', restaurant.*," +
		" address.*, contact.* FROM `table` t " +
		"INNER JOIN restaurant ON restaurant.id = t.restaurant_id " +
		"INNER JOIN address ON restaurant.address_id = address.id " +
		"INNER JOIN contact ON restaurant.contact_id = contact.id " +
		"WHERE t.id NOT IN (  " +
			"SELECT rt.table_id " +
			"FROM reservation r " +
			"INNER JOIN reservation_table rt ON r.id = rt.reservation_id " +
			"    INNER JOIN restaurant rest ON r.restaurant_id = rest.id " +
			"INNER JOIN status st ON r.status_id = st.id " +
			"WHERE r.restaurant_id = t.restaurant_id " +
			"AND st.name != 'Cancelled' " +
			"AND r.end_time > FROM_UNIXTIME(:r.requestedDate) " +
			"AND r.start_time < DATE_ADD(FROM_UNIXTIME(:r.requestedDate), INTERVAL rest.reservation_time HOUR) " +
		") " +
		" AND (:r.category.category = 0 OR restaurant.category_id = :r.category.category)"+
		"GROUP BY t.restaurant_id " +
		"HAVING availableCapacity >= :r.numPeople")
	@RegisterRowMapper(RestaurantSearchMapper.class)
	List<RestaurantSearchResponse> searchRestaurants(@BindBean("r")
                                    RestaurantSearchRequest request);

	class RestaurantSearchMapper implements RowMapper<RestaurantSearchResponse> {

		@Override
		public RestaurantSearchResponse map(ResultSet rs, StatementContext ctx) throws SQLException {
			RestaurantMapper restaurantMapper = new RestaurantMapper();
			return RestaurantSearchResponse.newBuilder()
				.setAvailableCapacity(rs.getInt("availableCapacity"))
				.setRestaurant(restaurantMapper.map(rs, ctx))
				.build();
		}
	}

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
				 .setId(rs.getInt("address.id"))
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
				 .setId(rs.getInt("contact.id"))
				 .setPhone(rs.getString("contact.phone"))
				 .setEmail(rs.getString("contact.email"))
				 .build())
				.setCategory(Category.newBuilder()
				 .setCategory(rs.getInt(
				              "restaurant.category_id"))
				 .build())
				.setCapacity(
				 rs.getInt("restaurant.capacity_factor"))
				.setRtime(
				 rs.getInt("restaurant.reservation_time"))
				.build();
		}
	}

	class RelationshipMapper implements RowMapper<Relationship> {
		@Override
		public Relationship map(ResultSet rs, StatementContext ctx)
		throws SQLException {
			return Relationship.newBuilder()
				// TODO: no, seriously
				.setRestaurant(Restaurant.newBuilder()
				 .setId(rs.getInt("restaurant.id"))
				 .setName(rs.getString("restaurant.name"))
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
				  .setPhone(rs.getString("rscon.phone"))
				  .setEmail(rs.getString("rscon.email"))
				  .build())
				 .setCategory(Category.newBuilder()
				  .setCategory(rs.getInt(
				               "restaurant.category_id"))
				  .build())
				 .build())
				.setUser(User.newBuilder()
				 .setId(rs.getInt("user.id"))
				 .setUsername(rs.getString("user.username"))
				 .setFname(rs.getString("user.fname"))
				 .setLname(rs.getString("user.lname"))
				 .setPoints(rs.getInt("user.rewards_points"))
				 .setContact(Contact.newBuilder()
				  .setPhone(rs.getString("uscon.phone"))
				  .setEmail(rs.getString("uscon.email"))
				  .build())
					//Todo: note that this will show user role as restaurant role
					//May want user role to actually be the true user role
				 .setRole(User.UserRole.valueOf(rs.getString("role").toUpperCase()))
				 .build())
				.setRole(Relationship.UserRole.valueOf(rs.getString("role").toUpperCase()))
				.build();

		}
	}
}


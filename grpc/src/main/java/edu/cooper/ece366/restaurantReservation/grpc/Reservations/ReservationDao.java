package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserDao.UserMapper;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface ReservationDao {
	@SqlQuery("select * from `table` t " +
			"where t.restaurant_id = :r.restaurant.id  " +
			"and t.id NOT IN ( " +
			"select rt.table_id " +
			"from reservation r " +
			"inner join reservation_table rt on r.id = rt.reservation_id " +
			"inner join status st on r.status_id = st.id " +
			"where r.restaurant_id = :r.restaurant.id " +
			"AND st.name != 'Cancelled' " +
			"AND r.end_time > FROM_UNIXTIME(:r.startTime) " +
			"AND r.start_time < FROM_UNIXTIME(:endTime) " +
			") ORDER BY t.capacity DESC")
	List<Table> getAvailableTables(@BindBean("r") Reservation reservation,
								   long endTime);

	//Reservation always starts with 0 points
	@SqlUpdate("INSERT INTO reservation" +
			"(start_time, end_time, num_people, num_points, restaurant_id," +
			" status_id)" +
			"VALUES(FROM_UNIXTIME(:r.startTime), FROM_UNIXTIME(:endTime), " +
			":r.numPeople, 0, :r.restaurant.id, :statusId)"
	)
	@GetGeneratedKeys("id")
	int insertReservation(@BindBean("r") Reservation reservation,
						  long endTime, int statusId);

	@SqlUpdate("UPDATE reservation set start_time = FROM_UNIXTIME(:r.startTime), " +
			"num_people = :r.numPeople, num_points = :r.points, " +
			"status_id  = :statusId WHERE id = :r.id")
	void updateReservation(@BindBean("r") Reservation reservation,
						   int statusId);

	@SqlBatch("INSERT INTO reservation_table(reservation_id, table_id) " +
			"VALUES(:resId, :t.id)")
	void insertReservationTables(int resId,
								 @BindBean("t") List<Table> tables);

	@SqlUpdate("INSERT INTO reservation_user(reservation_id, user_id)" +
			" VALUES(:resId, :userId)")
	void addReservationUser(int resId, int userId);

	@SqlUpdate("DELETE FROM reservation_user WHERE " +
			"reservation_id = :resId AND user_id = :userId")
	void removeReservationUser(int resId, int userId);

	@SqlQuery("SELECT u.id FROM reservation r " +
			"INNER JOIN reservation_user ru on r.id = ru.reservation_id " +
			"INNER JOIN user u on ru.user_id = u.id " +
			"WHERE r.id = :reservationId AND u.id = :userId")
	Optional<Integer> getReservationUser(int userId, int reservationId);

	@SqlQuery("SELECT u.id, u.username, u.fname, u.lname, " +
			"u.rewards_points as points, c.id, c.phone, c.email, ro.name as role " +
			"FROM reservation r " +
			"INNER JOIN reservation_user ru on r.id = ru.reservation_id " +
			"INNER JOIN user u on ru.user_id = u.id " +
			"INNER JOIN contact c on u.contact_id = c.id " +
			"INNER JOIN role ro on u.role_id = ro.id " +
			"WHERE r.id = :reservationId")
	@RegisterRowMapper(UserMapper.class)
	List<User> getReservationUsers(int reservationId);

	@SqlQuery("SELECT r.*, st.name as statusName, rest.*, a.*, c.* FROM reservation r " +
			"INNER JOIN status st on r.status_id = st.id " +
			"INNER JOIN restaurant rest on r.restaurant_id = rest.id " +
			"INNER JOIN address a on rest.address_id = a.id " +
			"INNER JOIN contact c on rest.contact_id = c.id " +
			"WHERE (:reservationId IS NULL OR r.id = :reservationId) AND " +
			"(:restaurantId IS NULL OR r.restaurant_id = :restaurantId) AND " +
			"(:userId IS NULL OR r.id IN (" +
			"SELECT ru.reservation_id FROM reservation_user ru " +
			"WHERE ru.reservation_id = r.id AND " +
			"ru.user_id = :userId)) AND " +
			"(:beginTime IS NULL OR (r.start_time >= FROM_UNIXTIME(:beginTime) AND " +
			"r.start_time <= FROM_UNIXTIME(:futureTime))) AND " +
			"(:statusId IS NULL OR r.status_id = :statusId) " +
			"ORDER BY r.start_time")
	@RegisterRowMapper(ReservationMapper.class)
	List<Reservation> searchReservations(Integer reservationId,
	                                     Integer userId,
	                                     Integer restaurantId,
	                                     Long beginTime,
	                                     Long futureTime,
	                                     Integer statusId);

	@SqlQuery("select t.id, t.capacity, t.label " +
			"from reservation r " +
			"INNER JOIN reservation_table rt on r.id = rt.reservation_id " +
			"INNER JOIN `table` t on rt.table_id = t.id " +
			"WHERE r.id = :reservationId")
	List<Table> getReservationTables(int reservationId);

	class ReservationMapper implements RowMapper<Reservation> {
		@Override
		public Reservation map(ResultSet rs, StatementContext ctx)
				throws SQLException {
			return Reservation.newBuilder()
					.setId(rs.getInt("id"))
					.setStartTime(rs.getTimestamp("start_time").getTime() / 1000)
					// Divide by 1000 since Java time is in milliseconds, Unix time is in seconds
					.setNumPeople(rs.getInt("num_people"))
					.setPoints(rs.getInt("num_points"))
					.setStatus(Reservation.ReservationStatus.valueOf(rs.getString("statusName").toUpperCase()))
					.setRestaurant(Restaurant.newBuilder()
							.setId(rs.getInt("restaurant_id"))
							.setName(rs.getString("rest.name"))
							.setAddress(Address.newBuilder()
									.setName(rs.getString("a.name"))
									.setLatitude(rs.getFloat("a.latitude"))
									.setLongitude(rs.getFloat("a.longitude"))
									.setLine1(rs.getString("a.line1"))
									.setLine2(rs.getString("a.line2"))
									.setCity(rs.getString("a.city"))
									.setState(rs.getString("a.state"))
									.setZip(rs.getString("a.zip"))
									.build())
							.setContact(Contact.newBuilder()
									.setEmail(rs.getString("c.email"))
									.setPhone(rs.getString("c.phone"))
									.build())
							.setCapacity(rs.getInt("rest.capacity_factor"))
							.setCategory(Category.newBuilder()
									.setCategory(rs.getInt("rest.category_id"))
									.build())
							.setRtime(rs.getInt("rest.reservation_time"))
							.build())
					.build();
		}
	}
}

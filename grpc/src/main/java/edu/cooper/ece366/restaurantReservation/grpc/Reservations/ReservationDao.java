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
		@SqlQuery("SELECT t.* FROM `table` t " +
			"LEFT JOIN (" +
				"SELECT rt.table_id AS tid, MAX(r.end_time) AS maxEnd " +
				"FROM reservation_table rt " +
				"INNER JOIN reservation r ON rt.reservation_id = r.id " +
				"WHERE r.end_time < FROM_UNIXTIME(:r.startTime) " +
				"GROUP BY tid) a ON t.id = a.tid " +
			"LEFT JOIN (" +
				"SELECT rt.table_id AS tid, MIN(r.start_time) AS minStart " +
				"FROM reservation_table rt " +
				"INNER JOIN reservation r ON rt.reservation_id = r.id " +
				"WHERE r.start_time > FROM_UNIXTIME(:endTime) " +
				"GROUP BY tid) b ON t.id = b.tid " +
			"WHERE t.restaurant_id = :r.restaurant.id  " +
			"AND t.id NOT IN (" +
				"SELECT rt.table_id " +
				"FROM reservation r " +
				"INNER JOIN reservation_table rt ON r.id = rt.reservation_id " +
				"INNER JOIN status st ON r.status_id = st.id " +
				"WHERE r.restaurant_id = :r.restaurant.id " +
				"AND st.name != 'Cancelled' " +
				"AND r.end_time > FROM_UNIXTIME(:r.startTime) " +
				"AND r.start_time < FROM_UNIXTIME(:endTime)) " +
			"AND t.capacity >= :requestCap " +
			"AND t.capacity <= :maxSize " +
			"ORDER BY t.capacity ASC," +
				"COALESCE(TIMESTAMPDIFF(MINUTE, a.maxEnd, FROM_UNIXTIME(:r.startTime)),0) " +
				"% (:reservationTime*60) " +
				"+ COALESCE(TIMESTAMPDIFF(MINUTE, FROM_UNIXTIME(:endTime), b.minStart),0) " +
				"% (:reservationTime*60) ASC " +
			"LIMIT 1")
	Optional<Table> getBestTable(@BindBean("r") Reservation reservation,
								   long endTime, int requestCap, int maxSize, int reservationTime);


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

	@SqlUpdate("UPDATE reservation SET start_time = FROM_UNIXTIME(:r.startTime), " +
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
			"INNER JOIN reservation_user ru ON r.id = ru.reservation_id " +
			"INNER JOIN user u ON ru.user_id = u.id " +
			"WHERE r.id = :reservationId AND u.id = :userId")
	Optional<Integer> getReservationUser(int userId, int reservationId);

	@SqlQuery("SELECT u.id, u.username, u.fname, u.lname, " +
			"u.rewards_points AS points, c.id, c.phone, c.email, ro.name AS role " +
			"FROM reservation r " +
			"INNER JOIN reservation_user ru ON r.id = ru.reservation_id " +
			"INNER JOIN user u ON ru.user_id = u.id " +
			"INNER JOIN contact c ON u.contact_id = c.id " +
			"INNER JOIN role ro ON u.role_id = ro.id " +
			"WHERE r.id = :reservationId")
	@RegisterRowMapper(UserMapper.class)
	List<User> getReservationUsers(int reservationId);

	@SqlQuery("SELECT r.*, st.name AS statusName, rest.*, a.*, c.* FROM reservation r " +
			"INNER JOIN status st ON r.status_id = st.id " +
			"INNER JOIN restaurant rest ON r.restaurant_id = rest.id " +
			"INNER JOIN address a ON rest.address_id = a.id " +
			"INNER JOIN contact c ON rest.contact_id = c.id " +
			"WHERE (:reservationId IS NULL OR r.id = :reservationId) AND " +
			"(:restaurantId IS NULL OR r.restaurant_id = :restaurantId) AND " +
			"(:userId IS NULL OR r.id IN (" +
			"SELECT ru.reservation_id FROM reservation_user ru " +
			"WHERE ru.reservation_id = r.id AND " +
			"ru.user_id = :userId)) AND " +
			"(:beginTime IS NULL OR (r.start_time >= FROM_UNIXTIME(:beginTime) AND " +
			"r.start_time <= DATE_ADD(FROM_UNIXTIME(:futureTime), INTERVAL 1 DAY))) AND " +
			"(:statusId IS NULL OR r.status_id = :statusId) " +
			"ORDER BY r.start_time")
	@RegisterRowMapper(ReservationMapper.class)
	List<Reservation> searchReservations(Integer reservationId,
	                                     Integer userId,
	                                     Integer restaurantId,
	                                     Long beginTime,
	                                     Long futureTime,
	                                     Integer statusId);

	@SqlQuery("SELECT t.id, t.capacity, t.label " +
			"FROM reservation r " +
			"INNER JOIN reservation_table rt ON r.id = rt.reservation_id " +
			"INNER JOIN `table` t ON rt.table_id = t.id " +
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

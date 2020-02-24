package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.Reservation;
import edu.cooper.ece366.restaurantReservation.grpc.Table;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;
import java.util.Optional;

public interface ReservationDao {
	//Todo: ignore cancelled reservations
	@SqlQuery("select * from `table` t " +
		"where t.restaurant_id = :r.restaurant.id  " +
		"and t.id NOT IN ( " +
			"select rt.table_id " +
			"from reservation r " +
			"inner join reservation_table rt on r.id = rt.reservation_id " +
			"where r.restaurant_id = :r.restaurant.id " +
			"AND r.end_time > FROM_UNIXTIME(:r.startTime) " +
			"AND r.start_time < FROM_UNIXTIME(:endTime) " +
		") ORDER BY t.capacity DESC")
	List<Table> getAvailableTables(@BindBean("r") Reservation reservation, long endTime);

	//Reservation always starts with 0 points
	@SqlUpdate("INSERT INTO reservation" +
		"(start_time, end_time, num_people, num_points, restaurant_id, status_id)"+
		"VALUES(FROM_UNIXTIME(:r.startTime), FROM_UNIXTIME(:endTime), :r.numPeople, 0, :r.restaurant.id, :statusId)"
	)
	@GetGeneratedKeys("id")
	int insertReservation(@BindBean("r") Reservation reservation, long endTime,
	                      int statusId);

	@SqlBatch("INSERT INTO reservation_table(reservation_id, table_id)" +
		" VALUES(:resId, :t.id)")
	void insertReservationTables(int resId, @BindBean("t") List<Table> tables);

	@SqlUpdate("INSERT INTO reservation_user(reservation_id, user_id)" +
		" VALUES(:resId, :userId)")
	void addReservationUser(int resId, int userId);

	@SqlQuery("SELECT u.id FROM reservation r " +
		"INNER JOIN reservation_user ru on r.id = ru.reservation_id " +
		"INNER JOIN user u on ru.user_id = u.id " +
		"WHERE r.id = :reservationId AND u.id = :userId")
	Optional<Integer> getReservationUser(int userId, int reservationId);

	@SqlQuery("SELECT u.id, u.username, u.fname, u.lname, " +
		"u.rewards_points as points, c.phone, c.email, r.name as role " +
		"FROM reservation r " +
		"INNER JOIN reservation_user ru on r.id = ru.reservation_id " +
		"INNER JOIN user u on ru.user_id = u.id " +
		"INNER JOIN contact c on c.id = u.contact_id " +
		"INNER JOIN role r on r.id = u.role_id " +
		"WHERE r.id = :reservationId")
	@RegisterRowMapper(UserMapper.class)
	List<User> getReservationUsers(int reservationId);

	@SqlQuery("SELECT r.*, st.name as statusName FROM reservation " +
		"INNER JOIN status st on r.status_id = st.id" +
		"WHERE (:reservationId IS NULL OR r.id = :reservationId) " +
		"(:restaurantId IS NULL OR r.restaurant_id = :restaurantId) " +
		"(:userId IS NULL OR r.id IN (" +
			"SELECT ru.reservation_id FROM reservation_user " +
			"WHERE ru.reservation_id = r.id AND ru.user_id = :userId))")
	@RegisterRowMapper(ReservationMapper.class)
	List<Reservation> searchReservations(Integer reservationId, Integer userId,
	                                     Integer restaurantId);

	@SqlQuery("select t.id, t.capacity, t.label " +
		"from reservation r " +
		"INNER JOIN reservation_table rt on r.id = rt.reservation_id " +
		"INNER JOIN `table` t on rt.table_id = t.id " +
		"WHERE r.id = :reservationId")
	List<Table> getReservationTables(int reservationId);
}

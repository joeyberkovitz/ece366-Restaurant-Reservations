package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.Reservation;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurant;
import edu.cooper.ece366.restaurantReservation.grpc.Table;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlBatch;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface ReservationDao {

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

	@SqlQuery("")
	Map<Integer, Restaurant> getRestaurantCapacities();
}

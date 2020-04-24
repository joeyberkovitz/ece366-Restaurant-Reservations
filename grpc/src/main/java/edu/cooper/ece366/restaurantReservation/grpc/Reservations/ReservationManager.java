package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.Reservation;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantDao;
import edu.cooper.ece366.restaurantReservation.grpc.StatusDao;
import edu.cooper.ece366.restaurantReservation.grpc.Table;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationManager {
	private Jdbi db;
	private List<Table> retTables;

	public ReservationManager(Jdbi db){
		this.db = db;
	}

	public Reservation createReservation(Reservation reservation, int userId){
		int reservationTime = db.withExtension(
			RestaurantDao.class,
			dao -> dao.getRestaurantReservationTime(reservation.getRestaurant().getId()));
		long endTime = reservation.getStartTime() + reservationTime*3600;
		int statusId = db.withExtension(StatusDao.class,
			dao -> dao.getStatusIdByName("Opened"));

		//This will throw a runtime exception if tables unavailable
		List<Table> tables = computeReservationTables(reservation, endTime, reservationTime);

		//Todo: do we want to put all inserts in a transaction?
		int reservationId = db.withExtension(ReservationDao.class,
			dao -> dao.insertReservation(reservation, endTime,
			                             statusId));

		db.useExtension(ReservationDao.class,
			dao -> dao.insertReservationTables(reservationId,
			                                   tables));

		db.useExtension(ReservationDao.class,
			dao -> dao.addReservationUser(reservationId, userId));

		return Reservation.newBuilder().setId(reservationId).build();
	}

	public void updateReservation(Reservation reservation){
		int statusId = db.withExtension(StatusDao.class,
			dao -> dao.getStatusIdByName(reservation.getStatus().name()));

		db.useExtension(ReservationDao.class,
			dao -> dao.updateReservation(reservation, statusId));
	}

	public void addReservationUser(int resId, String username)
			throws UserManager.InvalidUsernameException {
		UserManager um = new UserManager(db);
		int userId = um.getIdByUsername(username);

		db.useExtension(ReservationDao.class,
			dao -> dao.addReservationUser(resId, userId));
	}

	public void removeReservationUser(int resId, int userId) {
		db.useExtension(ReservationDao.class,
			dao -> dao.removeReservationUser(resId, userId));
	}

	public List<User> getReservationUsers(int resId) {
		return db.withExtension(ReservationDao.class, dao -> {
			return dao.getReservationUsers(resId);
		});
	}

	public List<Table> getReservationTables(int resId) {
		return db.withExtension(ReservationDao.class, dao -> {
			return dao.getReservationTables(resId);
		});
	}

	public List<Reservation> searchReservations(Integer resId,
	                                            Integer userId,
	                                            Integer restId,
	                                            Long beginTime,
	                                            Long futureTime,
	                                            String status) {
		Integer statusId;
		if(status != null) {
			if(status.equals("ALL"))
				statusId = null;
			else
				statusId = db.withExtension(StatusDao.class,
					dao -> dao.getStatusIdByName(status));
		}
		else
			statusId = null;
		return db.withExtension(ReservationDao.class, dao -> {
			return dao.searchReservations(resId, userId, restId, beginTime, futureTime, statusId);
		});
	}

	/* Reservation Algorithm: First attempt to seat users at one table with capacity greater than or equal to the number
	of participants of the reservation and less than or equal to the number of participants of the reservation divided
	by the restaurant's table capacity factor. The smallest table capacity is preferred.
	Next, if this is not possible, decrease the current target table capacity (initially set to the number of
	participants of the reservation) by one and try again. Keep doing this until a table is found. When a table is
	found, decrease the overall target table capacity (also initially set to the number of participants of the
	reservation) by that table's capacity and recursively find all tables necessary for the reservation. If at any point
	the target table capacity is less than or equal to zero, then that means all participants have been seated and those
	tables are returned. If, however, the target table capacity is greater than zero but the current target table
	capacity is zero, that means sufficient tables were not found.
	Note the table capacity factor applies to the entire set of tables in a reservation, not each individual table. The
	maxSize variable is used to either (1) limit table searching by the restaurant's table capacity factor or (2) limit
	table searching because from a previous attempt it is known no tables exist.
	Obviously some restaurants may have multiple tables with the same capacity. To choose from among these tables, the
	table with the shortest "dead time" is chosen. A table's "dead time" is the time from the table's previous
	reservation to this reservation's start time modulo the normal reservation length (set by the restaurant) plus the
	time from the end of this reservation to the start of the next reservation modulo the normal reservation length.
	This maximizes the amount of reservations a restaurant can serve.
	 */
	private List<Table> computeReservationTables(Reservation reservation, long endTime, int reservationTime){
		int capFactor = db.withExtension(RestaurantDao.class,
				dao -> dao.getRestaurantCapFactor(reservation.getRestaurant().getId()));

		int requestCap = reservation.getNumPeople();
		int maxSize = requestCap*100/capFactor;

		retTables = new ArrayList<Table>();

		getBestTable(reservation, requestCap, requestCap, maxSize, maxSize, endTime, reservationTime);

		return retTables;
	}

	private void getBestTable(Reservation reservation, int target, int currTarget, int maxSize, int overallMaxSize,
							  long endTime, int reservationTime){
		if (target <= 0)
			return;

		if (currTarget == 0)
			throw new StatusRuntimeException(Status.ABORTED
					.withDescription("Requested capacity unavailable"));

		int finalCurrTarget = currTarget;
		int finalMaxSize = maxSize;
		Optional<Table> table = db.withExtension(ReservationDao.class,
				dao -> dao.getBestTable(reservation, endTime, finalCurrTarget, finalMaxSize, reservationTime));

		if (table.isPresent()) {
			retTables.add(table.get());

			target -= table.get().getCapacity();
			currTarget = target;
			overallMaxSize -= table.get().getCapacity();
			maxSize = Math.min(overallMaxSize, table.get().getCapacity());
		}
		else {
			currTarget--;
			maxSize = currTarget;
		}

		getBestTable(reservation, target, currTarget, maxSize, overallMaxSize, endTime, reservationTime);
	}

	public boolean canEditReservation(int userId, Reservation reservation){
		Optional<Integer> resUser = db.withExtension(ReservationDao.class,
			dao -> dao.getReservationUser(userId, reservation.getId()));

		Optional<String> restaurantUser = Optional.empty();
		if (resUser.isEmpty()){
			// If current user is not on reservation, check if user is manager of restaurant of reservation.
			// Managers have privileges over all their restaurant's reservations
			restaurantUser = db.withExtension(RestaurantDao.class,
				dao -> dao.getRestaurantUserRole(userId,
					reservation.getRestaurant().getId()));

		}

		return resUser.isPresent() || (restaurantUser.isPresent() && (
			restaurantUser.get().equals("Admin") ||
			restaurantUser.get().equals("Manager")
		));
	}
}

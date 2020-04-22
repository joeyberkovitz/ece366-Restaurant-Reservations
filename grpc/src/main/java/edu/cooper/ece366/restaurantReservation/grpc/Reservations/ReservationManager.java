package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantDao;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationManager {
	private Jdbi db;

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
		List<Table> tables = computeReservationTables(reservation, endTime);

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


	private List<Table> computeReservationTables(Reservation reservation, long endTime){
		//Todo: this algorithm may be too simple, always chooses largest table first
		List<Table> tables = db.withExtension(ReservationDao.class,
			dao -> dao.getAvailableTables(reservation, endTime));
		int capFactor = db.withExtension(RestaurantDao.class,
			dao -> dao.getRestaurantCapFactor(reservation.getRestaurant().getId()));

		List<Table> retTables = new ArrayList<Table>();

		int requestCap = reservation.getNumPeople();
		while(requestCap > 0){
			if(tables.size() == 0){
				throw new StatusRuntimeException(Status.ABORTED
					.withDescription("Requested capacity unavailable"));
			}
			Table currTable = tables.remove(0);
			if(requestCap >= currTable.getCapacity()*capFactor/100) {
				retTables.add(currTable);
				requestCap -= currTable.getCapacity();
			}
		}

		return retTables;
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

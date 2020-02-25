package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.Reservation;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantDao;
import edu.cooper.ece366.restaurantReservation.grpc.StatusDao;
import edu.cooper.ece366.restaurantReservation.grpc.Table;
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
		int reservationTime = db.withExtension(RestaurantDao.class, d ->
			d.getRestaurantReservationTime(reservation.getRestaurant().getId()));
		long endTime = reservation.getStartTime() + reservationTime*3600;
		int statusId = db.withExtension(StatusDao.class,
			d -> d.getStatusIdByName("Opened"));

		//This will throw a runtime exception if tables unavailable
		List<Table> tables = computeReservationTables(reservation, endTime);

		//Todo: do we want to put all inserts in a transaction?
		int reservationId = db.withExtension(ReservationDao.class,
			d -> d.insertReservation(reservation, endTime, statusId));

		db.useExtension(ReservationDao.class,
			d -> d.insertReservationTables(reservationId, tables));

		db.useExtension(ReservationDao.class,
			d -> d.addReservationUser(reservationId, userId));

		return Reservation.newBuilder().setId(reservationId).build();
	}

	public void updateReservation(Reservation reservation){
		int statusId = db.withExtension(StatusDao.class,
			d -> d.getStatusIdByName(reservation.getStatus().name()));

		db.useExtension(ReservationDao.class,
			d -> d.updateReservation(reservation, statusId));
	}

	private List<Table> computeReservationTables(Reservation reservation, long endTime){
		//Todo: this algorithm may be too simple, always chooses largest table first
		List<Table> tables = db.withExtension(ReservationDao.class,
			d -> d.getAvailableTables(reservation, endTime));
		int capFactor = db.withExtension(RestaurantDao.class,
			d -> d.getRestaurantCapFactor(reservation.getRestaurant().getId()));

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
			d -> d.getReservationUser(userId, reservation.getId()));

		Optional<String> restaurantUser = Optional.empty();
		if(resUser.isEmpty()){
		// TODO when would this return empty? What is being returned?
			restaurantUser = db.withExtension(RestaurantDao.class,
				d -> d.getRestaurantUserRole(userId,
					reservation.getRestaurant().getId()));

		}

		return resUser.isPresent() || (restaurantUser.isPresent() && (
			restaurantUser.get().equals("Admin") ||
			restaurantUser.get().equals("Manager")
		));
	}
}

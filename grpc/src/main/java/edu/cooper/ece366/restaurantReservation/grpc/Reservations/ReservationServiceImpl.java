package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

import java.util.List;

public class ReservationServiceImpl extends ReservationServiceGrpc.ReservationServiceImplBase {

	private Jdbi db;
	private ReservationManager manager;

	public ReservationServiceImpl(Jdbi db) {
		this.db = db;
		this.manager = new ReservationManager(db);
	}

	@Override
	public void createReservation(Reservation request, StreamObserver<Reservation> responseObserver) {
		//Todo: maybe do some validation here

		//Need user ID to associate reservation to user
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());

		//Manager will only create reservation if tables are available
		Reservation createdRes = manager.createReservation(request, userId);

		responseObserver.onNext(createdRes);
		responseObserver.onCompleted();
	}

	@Override
	public StreamObserver<InviteMessage> inviteReservation(StreamObserver<InviteResponse> responseObserver) {
		return new StreamObserver<InviteMessage>() {
			@Override
			public void onNext(InviteMessage value) {
				checkReservationPermission(value.getReservation());

				db.useExtension(ReservationDao.class,
					d -> d.addReservationUser(value.getReservation().getId(),
						value.getUser().getId()));
			}

			@Override
			public void onError(Throwable t) {
				responseObserver.onError(t);
			}

			@Override
			public void onCompleted() {
				responseObserver.onNext(InviteResponse.newBuilder().build());
			}
		};
	}

	@Override
	public void listReservationUsers(Reservation request, StreamObserver<User> responseObserver) {
		checkReservationPermission(request);

		List<User> users = db.withExtension(ReservationDao.class,
			d -> d.getReservationUsers(request.getId()));

		for (User user: users) {
			responseObserver.onNext(user);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationsByUser(ReservationUserRequest request, StreamObserver<Reservation> responseObserver) {
		int currUser = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		//No need for auth check because only getting reservations for currUser
		List<Reservation> reservations = db.withExtension(ReservationDao.class,
			d -> d.searchReservations(null, currUser, null));

		for (Reservation reservation: reservations) {
			responseObserver.onNext(reservation);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservation(Reservation request, StreamObserver<Reservation> responseObserver) {
		//This function will guarantee that the reservation exists
		checkReservationPermission(request);

		List<Reservation> reservations = db.withExtension(ReservationDao.class,
			d -> d.searchReservations(request.getId(), null, null));

		responseObserver.onNext(reservations.get(0));
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationsByRestaurant(Restaurant request, StreamObserver<Reservation> responseObserver) {
		RestaurantManager restaurantManager = new RestaurantManager(db);
		int currUser = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(restaurantManager.canEditRestaurant(currUser, request.getId(), false)){
			throw new StatusRuntimeException(Status.PERMISSION_DENIED
				.withDescription("Not allowed to access restaurant"));
		}

		List<Reservation> reservations = db.withExtension(ReservationDao.class,
			d -> d.searchReservations(null, null, request.getId()));

		for (Reservation reservation: reservations) {
			responseObserver.onNext(reservation);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationTables(Reservation request, StreamObserver<Table> responseObserver) {
		checkReservationPermission(request);

		List<Table> tables = db.withExtension(ReservationDao.class,
			d -> d.getReservationTables(request.getId()));

		for (Table table: tables) {
			responseObserver.onNext(table);
		}
		responseObserver.onCompleted();
	}

	private void checkReservationPermission(Reservation reservation){
		int currUser = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!manager.canEditReservation(currUser, reservation)){
			throw new StatusRuntimeException(Status.PERMISSION_DENIED
				.withDescription("User can't edit reservation"));
		}
	}
}

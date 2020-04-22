package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
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
	public void inviteReservation(InviteMessage request, StreamObserver<InviteResponse> responseObserver) {
		checkReservationPermission(request.getReservation());

		try {
			manager.addReservationUser(request.getReservation().getId(),
				request.getUser().getUsername());
		} catch (UserManager.InvalidUsernameException e) {
			e.printStackTrace();
			throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid username."));
		}

		responseObserver.onNext(InviteResponse.newBuilder().build());
		responseObserver.onCompleted();
	}

	@Override
	public void removeReservationUser(InviteMessage request, StreamObserver<InviteResponse> responseObserver) {
		checkReservationPermission(request.getReservation());

		manager.removeReservationUser(request.getReservation().getId(),
			request.getUser().getId());

		responseObserver.onNext(InviteResponse.newBuilder().build());
		responseObserver.onCompleted();
	}

	@Override
	public void listReservationUsers(Reservation request, StreamObserver<User> responseObserver) {
		checkReservationPermission(request);

		List<User> users = manager.getReservationUsers(request.getId());

		for (User user: users) {
			responseObserver.onNext(user);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationsByUser(ReservationUserRequest request, StreamObserver<Reservation> responseObserver) {
		int currUser = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		//No need for auth check because only getting reservations for currUser
		// TODO do we need to allow admins to request reservation lists
		// of arbitrary users from the frontend?
		List<Reservation> reservations = manager.searchReservations(null, currUser, null,
				request.getBeginTime(), request.getFutureTime(), request.getStatus());

		for (Reservation reservation: reservations) {
			responseObserver.onNext(reservation);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservation(Reservation request, StreamObserver<Reservation> responseObserver) {
		//This function will guarantee that the reservation exists
		checkReservationPermission(request);

		List<Reservation> reservations = manager.searchReservations(request.getId(), null, null,
				null, null, null);

		responseObserver.onNext(reservations.get(0));
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationsByRestaurant(ReservationRestaurantRequest request, StreamObserver<Reservation> responseObserver) {
		RestaurantManager restaurantManager = new RestaurantManager(db);
		int currUser = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!restaurantManager.canEditRestaurant(currUser, request.getRestaurant().getId(), false)){
			throw new StatusRuntimeException(Status.PERMISSION_DENIED
				.withDescription("Not allowed to access restaurant"));
		}

		List<Reservation> reservations = manager.searchReservations(null, null,
				request.getRestaurant().getId(), request.getBeginTime(), request.getFutureTime(),
				request.getStatus());

		for (Reservation reservation: reservations) {
			responseObserver.onNext(reservation);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationTables(Reservation request, StreamObserver<Table> responseObserver) {
		checkReservationPermission(request);

		List<Table> tables = manager.getReservationTables(request.getId());

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

	@Override
	public void setReservation(Reservation request, StreamObserver<Reservation> responseObserver) {
		//After calling this, reservation is guaranteed to exist
		checkReservationPermission(request);

		List<Reservation> reservationList = manager.searchReservations(request.getId(), null, null,
				null, null, null);

		Reservation originalReservation = reservationList.get(0);

		if(request.getNumPeople() > originalReservation.getNumPeople())
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT
				.withDescription("Increasing num people not supported"));
		else if(request.getNumPeople() < originalReservation.getNumPeople()){
			//Todo: need to figure out if tables should be removed
		}

		if(request.getPoints() != originalReservation.getPoints())
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT
				.withDescription("Not allowed to modify points"));

		if(request.getStartTime() != originalReservation.getStartTime())
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT
				.withDescription("Not allowed to change start time"));

		if(request.getRestaurant().getId() !=
			originalReservation.getRestaurant().getId())
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT
				.withDescription("Not allowed to change restaurant"));

		manager.updateReservation(request);

		List<Reservation> reservation = manager.searchReservations(request.getId(), null, null,
				null, null, null);

		responseObserver.onNext(reservation.get(0));
		responseObserver.onCompleted();
	}
}

package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

import java.util.ArrayList;
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
		//Need user ID to associate reservation to user
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());

		//Manager will only create reservation if tables are available
		Reservation createdRes;
		try {
			createdRes = manager.createReservation(request, userId);
		}
		catch (ReservationManager.InvalidReservationException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.DATA_LOSS.withDescription("Unknown reservation error")));
			return;
		}
		catch (ReservationManager.InvalidStartTimeException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Start time cannot be earlier than current time")));
			return;
		}
		catch (ReservationManager.InvalidRestaurantException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Unknown restaurant error")));
			return;
		}
		catch (ReservationManager.InvalidNumPeopleException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Number of participants must be positive")));
			return;
		}

		responseObserver.onNext(createdRes);
		responseObserver.onCompleted();
	}

	@Override
	public void inviteReservation(InviteMessage request, StreamObserver<InviteResponse> responseObserver) {
		try {
			manager.checkReservationPermission(request.getReservation());
		}
		catch (ReservationManager.ReservationUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit reservation")));
			return;
		}

		try {
			manager.addReservationUser(request.getReservation().getId(),
				request.getUser().getUsername());
		}
		catch (UserManager.InvalidUsernameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid username")));
			return;
		}

		responseObserver.onNext(InviteResponse.newBuilder().build());
		responseObserver.onCompleted();
	}

	@Override
	public void removeReservationUser(InviteMessage request, StreamObserver<InviteResponse> responseObserver) {
		try {
			manager.checkReservationPermission(request.getReservation());
		}
		catch (ReservationManager.ReservationUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit reservation")));
			return;
		}

		manager.removeReservationUser(request.getReservation().getId(),
			request.getUser().getId());

		responseObserver.onNext(InviteResponse.newBuilder().build());
		responseObserver.onCompleted();
	}

	@Override
	public void listReservationUsers(Reservation request, StreamObserver<User> responseObserver) {
		try {
			manager.checkReservationPermission(request);
		} catch (ReservationManager.ReservationUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit reservation")));
			return;
		}

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
		List<Reservation> reservations = manager.searchReservations(null, currUser, null, null,
				request.getBeginTime(), request.getFutureTime(), request.getStatus());

		for (Reservation reservation: reservations) {
			responseObserver.onNext(reservation);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservation(Reservation request, StreamObserver<Reservation> responseObserver) {
		//This function will guarantee that the reservation exists
		try {
			manager.checkReservationPermission(request);
		}
		catch (ReservationManager.ReservationUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit reservation")));
			return;
		}

		List<Reservation> reservations = manager.searchReservations(request.getId(), null, null,
				null, null, null, null);

		responseObserver.onNext(reservations.get(0));
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationsByRestaurant(ReservationRestaurantRequest request, StreamObserver<Reservation> responseObserver) {
		List<Reservation> reservations = new ArrayList<Reservation>();

		try {
			reservations = manager.getReservationsByRestaurant(request.getRestaurant(),
					request.getBeginTime(), request.getFutureTime(), request.getStatus());
		}
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to view restaurant's reservations")));
			return;
		}
		// should never occur
		catch (RestaurantManager.InvalidTableIdException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.UNKNOWN.withDescription("Unknown error occurred")));
			return;
		}
		catch (RestaurantManager.InvalidRestException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unknown restaurant error")));
			return;
		}

		for (Reservation reservation: reservations) {
			responseObserver.onNext(reservation);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getReservationTables(Reservation request, StreamObserver<Table> responseObserver) {
		try {
			manager.checkReservationPermission(request);
		}
		catch (ReservationManager.ReservationUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit reservation")));
			return;
		}

		List<Table> tables = manager.getReservationTables(request.getId());

		for (Table table: tables) {
			responseObserver.onNext(table);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void setReservation(Reservation request, StreamObserver<Reservation> responseObserver) {
		//After calling this, reservation is guaranteed to exist
		try {
			manager.checkReservationPermission(request);
		}
		catch (ReservationManager.ReservationUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit reservation")));
			return;
		}

		List<Reservation> reservationList = manager.searchReservations(request.getId(), null, null,
				null, null, null, null);

		Reservation originalReservation = reservationList.get(0);

		if(request.getNumPeople() != originalReservation.getNumPeople()) {
			responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Changing the number of people not supported")));
			return;
		}

		if(request.getPoints() != originalReservation.getPoints()) {
			responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Not allowed to modify points")));
			return;
		}

		if(request.getStartTime() != originalReservation.getStartTime()) {
			responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Not allowed to change start time")));
			return;
		}

		if(request.getRestaurant().getId() !=
			originalReservation.getRestaurant().getId()) {
			responseObserver.onError(new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Not allowed to change restaurant")));
			return;
		}

		manager.updateReservation(request, request.getStatus().name());

		List<Reservation> reservation = manager.searchReservations(request.getId(), null, null,
				null, null, null, null);

		responseObserver.onNext(reservation.get(0));
		responseObserver.onCompleted();
	}
}

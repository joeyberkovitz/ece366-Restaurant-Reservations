package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Reservation;
import edu.cooper.ece366.restaurantReservation.grpc.ReservationServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

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
}

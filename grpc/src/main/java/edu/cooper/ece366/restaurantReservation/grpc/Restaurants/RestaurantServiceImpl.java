package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

public class RestaurantServiceImpl extends RestaurantServiceGrpc.RestaurantServiceImplBase {
	private Jdbi db;
	private RestaurantManager manager;
	public RestaurantServiceImpl(Jdbi db) {
		this.db = db;
		this.manager = new RestaurantManager(db);
	}

	@Override
	public void createRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		try {
			int restId = manager.checkAndInsertRestaurant(req);
			Restaurant reply =
			Restaurant.newBuilder().setId(restId).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
		catch (ContactManager.InvalidContactIdException |
		       ContactManager.InvalidPhoneException |
		       ContactManager.InvalidEmailException |
		       AddressManager.InvalidAddressIdException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void getRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		responseObserver.onNext(manager.getRestaurant(req.getId()));
		responseObserver.onCompleted();
	}

	@Override
	public void setRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		try {
			responseObserver.onNext(manager.setRestaurant(req));
			responseObserver.onCompleted();
		}
		catch (ContactManager.InvalidContactIdException |
		       ContactManager.InvalidPhoneException |
		       ContactManager.InvalidEmailException |
		       AddressManager.InvalidAddressIdException e) {
			e.printStackTrace();
		}
	}
}

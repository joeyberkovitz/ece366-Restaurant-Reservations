package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import io.grpc.Context;
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
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		System.out.println("User id is: " + userId);
		try {
			int restId = manager.checkAndInsertRestaurant(req);

			// Insert current user as admin (owner) for new restaurant
			int adminRoleId = db.withExtension(RoleDao.class, d -> d.getRoleIdByName("Admin"));
			db.useExtension(RestaurantDao.class, d -> d.addRestaurantRelationship(restId, userId, adminRoleId));

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
}

package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

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
			try {
				db.useExtension(RestaurantDao.class, d -> d.addRestaurantRelationship(restId, userId, adminRoleId));
			}
			catch (UnableToExecuteStatementException ex){
				ex.printStackTrace();

			}
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
	public void setRelationship(Relationship request, StreamObserver<RelationshipResponse> responseObserver) {
		if(!request.hasRestaurant() || request.getRestaurant().getId() == 0 || request.getUser().getId() == 0) {
			responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid Restaurant")));
			return;
		}

		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!manager.canEditRestaurant(userId, request.getRestaurant().getId(), false)) {
			responseObserver.onError(new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("Not authorized to " +
					"edit restaurant")));
			return;
		}

		String roleName = request.getRole().getValueDescriptor().getName();
		int roleId = db.withExtension(RoleDao.class, d -> d.getRoleIdByName(roleName));

		try {
			db.useExtension(RestaurantDao.class, d -> {
					d.addRestaurantRelationship(request.getRestaurant().getId(), request.getUser().getId(), roleId);
			});
		}
		catch (UnableToExecuteStatementException ex){
			ex.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("Relationship already exists or invalid key")));
			return;
		}

		RelationshipResponse res = RelationshipResponse.newBuilder().build();
		responseObserver.onNext(res);
		responseObserver.onCompleted();
	}
}

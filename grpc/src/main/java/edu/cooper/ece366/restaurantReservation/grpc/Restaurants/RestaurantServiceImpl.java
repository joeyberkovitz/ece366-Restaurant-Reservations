package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.util.List;
import java.util.Optional;

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
	public void getRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		responseObserver.onNext(manager.getRestaurant(req.getId()));
		responseObserver.onCompleted();
	}

	@Override
	public void setRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!manager.canEditRestaurant(userId, req.getId(), false)) {
			responseObserver.onError(
				new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit restaurant")));
			return;
		}

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

	@Override
	public void setRelationship(Relationship request, StreamObserver<RelationshipResponse> responseObserver) {
		if(!request.hasRestaurant() || request.getRestaurant().getId() == 0
			|| request.getUser().getId() == 0) {
			responseObserver.onError(new StatusRuntimeException(
				Status.NOT_FOUND.withDescription("Invalid Restaurant")));
			return;
		}

		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!manager.canEditRestaurant(userId, request.getRestaurant().getId(), false)) {
			responseObserver.onError(
				new StatusRuntimeException(Status.PERMISSION_DENIED
				.withDescription("Not authorized to edit restaurant")));
			return;
		}

		String roleName = request.getRole().getValueDescriptor().getName();
		int roleId = db.withExtension(RoleDao.class,
			d -> d.getRoleIdByName(roleName));

		try {
			db.useExtension(RestaurantDao.class, d -> {
				d.addRestaurantRelationship(request.getRestaurant().getId(),
					request.getUser().getId(), roleId);
			});
		}
		catch (UnableToExecuteStatementException ex){
			ex.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
				Status.ALREADY_EXISTS
				.withDescription("Relationship already exists or invalid key")));
			return;
		}

		RelationshipResponse res = RelationshipResponse.newBuilder().build();
		responseObserver.onNext(res);
		responseObserver.onCompleted();
	}

	@Override
	public void createTable(CreateTableRequest request, StreamObserver<Table> responseObserver) {
		if(!request.hasTable() || !request.hasTarget() ||
			request.getTable().getCapacity() <= 0 ||
			request.getTable().getLabel().isEmpty() ||
			request.getTarget().getId() == 0){
			responseObserver.onError(
				new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Missing restaurant or table details")));
			return;
		}

		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!manager.canEditRestaurant(userId, request.getTarget().getId(), false)) {
			responseObserver.onError(
				new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit restaurant")));
			return;
		}

		int tableId;
		try {
			tableId = manager.checkAndInsertTable(request.getTable(),
				request.getTarget());
		} catch (RestaurantManager.InvalidTableException e) {
			responseObserver.onError(
				new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Invalid table name or table already exists"))
			);
			return;
		}
		responseObserver.onNext(db.withExtension(RestaurantDao.class,
			d -> d.getTableById(tableId)));
		responseObserver.onCompleted();
	}

	@Override
	public void getTableById(Table request, StreamObserver<Table> responseObserver) {
		//Todo: do we want to check if user has permission to restaurant associated with table
		if(request.getId() == 0){
			responseObserver.onError(
				new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Invalid table ID")));
			return;
		}

		//Todo: do we want to handle invalid id with an Optional?
		Table table = db.withExtension(RestaurantDao.class,
			d -> d.getTableById(request.getId()));

		responseObserver.onNext(table);
		responseObserver.onCompleted();
	}

	@Override
	public void getTablesByRestaurant(Restaurant request, StreamObserver<Table> responseObserver) {
		if(request.getId() == 0){
			responseObserver.onError(
				new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Missing restaurant id")));
			return;
		}

		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(!manager.canEditRestaurant(userId, request.getId(), false)) {
			responseObserver.onError(
				new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit restaurant")));
			return;
		}

		List<Table> tables = manager.getRestaurantTables(request.getId());
		for (Table table: tables) {
			responseObserver.onNext(table);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void setTable(Table request, StreamObserver<Table> responseObserver) {
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		Optional<Restaurant> restaurant = manager.getRestaurantByTable(request.getId());
		if(restaurant.isEmpty() || !manager.canEditRestaurant(userId,
			restaurant.get().getId(), false)) {
			responseObserver.onError(
				new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit restaurant")));
			return;
		}

		Table retTable;
		try{
			retTable = manager.setTable(request, restaurant.get());
		} catch (RestaurantManager.InvalidTableException e) {
			e.printStackTrace();
			responseObserver.onError(
				new StatusRuntimeException(Status.INVALID_ARGUMENT
					.withDescription("Invalid table parameters received"))
			);
			return;
		}

		responseObserver.onNext(retTable);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteTable(Table request, StreamObserver<DeleteTableResponse> responseObserver) {
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		Optional<Restaurant> restaurant = manager.getRestaurantByTable(request.getId());
		if(restaurant.isEmpty() || !manager.canEditRestaurant(userId,
			restaurant.get().getId(), false)) {
			responseObserver.onError(
				new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit restaurant")));
			return;
		}

		DeleteTableResponse deleteTableResponse = DeleteTableResponse
			.newBuilder().build();

		manager.deleteTableById(request.getId());

		responseObserver.onNext(deleteTableResponse);
		responseObserver.onCompleted();
	}
}

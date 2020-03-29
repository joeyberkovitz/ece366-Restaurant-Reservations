package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Role.RoleManager;
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
			RoleManager rm = new RoleManager(db);
			int adminRoleId = rm.getRoleById("Admin");

			manager.addRestaurantRelationship(restId, userId, adminRoleId);

			Restaurant reply =
			Restaurant.newBuilder().setId(restId).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
		catch (RestaurantManager.InvalidRestNameException |
				ContactManager.InvalidContactIdException |
				ContactManager.InvalidPhoneException |
				ContactManager.InvalidEmailException |
				AddressManager.InvalidAddrFormException |
				AddressManager.InvalidLatException |
				AddressManager.InvalidLongException |
				AddressManager.InvalidAddrNameException |
				AddressManager.InvalidZipException e) {
			e.printStackTrace();
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Restaurant details are invalid"));
		}
		catch (UnableToExecuteStatementException ex){
			// todo include StatusRuntimeException?
			ex.printStackTrace();
		}
	}

	@Override
	public void getRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		Restaurant restaurant = manager.getRestaurant(req.getId());
		if(restaurant == null){
			throw new StatusRuntimeException(Status.NOT_FOUND
				.withDescription("Unable to find requested restaurant"));
		}

		responseObserver.onNext(restaurant);
		responseObserver.onCompleted();
	}

	@Override
	public void setRestaurant(Restaurant req, StreamObserver<Restaurant> responseObserver) {
		checkRestaurantPermission(req, null, false);

		try {
			responseObserver.onNext(manager.setRestaurant(req));
			responseObserver.onCompleted();
		}
		catch (RestaurantManager.InvalidRestNameException |
				ContactManager.InvalidContactIdException |
				ContactManager.InvalidPhoneException |
				ContactManager.InvalidEmailException |
				AddressManager.InvalidAddressIdException |
				AddressManager.InvalidAddrFormException |
				AddressManager.InvalidLatException |
				AddressManager.InvalidLongException |
				AddressManager.InvalidAddrNameException |
				AddressManager.InvalidZipException e) {
			e.printStackTrace();
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Restaurant details are invalid"));
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

		checkRestaurantPermission(request.getRestaurant(), null, true);

		RoleManager rm = new RoleManager(db);
		String roleName = request.getRole().getValueDescriptor().getName();
		int roleId = rm.getRoleById(roleName);

		try {
			manager.addRestaurantRelationship(request.getRestaurant().getId(),
				request.getUser().getId(), roleId);
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

		checkRestaurantPermission(request.getTarget(), null, false);

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
		responseObserver.onNext(this.manager.getTableById(tableId));
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
		Table table = this.manager.getTableById(request.getId());

		responseObserver.onNext(table);
		responseObserver.onCompleted();
	}

	@Override
	public void searchByCategory(Category request, StreamObserver<Restaurant> responseObserver) {
		List<Restaurant> results = manager.searchByCategory(request);
		for(Restaurant result: results) {
			responseObserver.onNext(result);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getUsersByRestaurant(Restaurant request, StreamObserver<Relationship> responseObserver) {
		List<Relationship> results = manager.getRelationshipByRestaurant(request);
		for(Relationship result: results) {
			responseObserver.onNext(result);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getRestaurantsByUser(User request, StreamObserver<Relationship> responseObserver) {
		List<Relationship> results = manager.getRelationshipByUser(request);
		for(Relationship result: results) {
			responseObserver.onNext(result);
		}
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

		checkRestaurantPermission(request, null, false);

		List<Table> tables = manager.getRestaurantTables(request.getId());
		for (Table table: tables) {
			responseObserver.onNext(table);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void setTable(Table request, StreamObserver<Table> responseObserver) {
		Restaurant restaurant = checkRestaurantPermission(null, request, false);

		Table retTable;
		try{
			retTable = manager.setTable(request, restaurant);
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
		//todo lazy delete if there were reservations
		checkRestaurantPermission(null, request, false);

		DeleteTableResponse deleteTableResponse = DeleteTableResponse
			.newBuilder().build();

		manager.deleteTableById(request.getId());

		responseObserver.onNext(deleteTableResponse);
		responseObserver.onCompleted();
	}

	private Restaurant checkRestaurantPermission(Restaurant rest, Table tab, boolean priv){
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());
		if(rest == null && tab != null && tab.getId() > 0) {
			Optional<Restaurant> restaurant = manager.getRestaurantByTable(tab.getId());
			if(restaurant.isPresent())
				rest = restaurant.get();
		}
		if(rest == null || rest.getId() <= 0 || !manager.canEditRestaurant(userId,
			rest.getId(), priv)) {
			// Check null first to ensure restaurant object exists
			throw new StatusRuntimeException(Status.PERMISSION_DENIED
					.withDescription("Not authorized to edit restaurant"));
		}

		return rest;
	}

	@Override
	public void deleteRestaurant(Restaurant rest, StreamObserver<DeleteRestaurantResponse> responseObserver){
		//Deleting a restaurant should be a privileged operation
		checkRestaurantPermission(rest, null, true);

		DeleteRestaurantResponse deleteRestaurantResponse = DeleteRestaurantResponse
				.newBuilder().build();

		try {
			manager.deleteRestaurant(rest.getId());
		} catch (AddressManager.InvalidAddressIdException | ContactManager.InvalidContactIdException e) {
			e.printStackTrace();
			throw new StatusRuntimeException(Status.INTERNAL.withDescription("An error occurred."));
		}

		responseObserver.onNext(deleteRestaurantResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void getCategoryList(GetCategoryRequest request, StreamObserver<Category> responseObserver) {
		List<Category> categories = manager.getCategories();
		for(Category category: categories){
			responseObserver.onNext(category);
		}
		responseObserver.onCompleted();
	}
}

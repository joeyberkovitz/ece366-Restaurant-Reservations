package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Role.RoleManager;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;

import java.util.List;

public class RestaurantServiceImpl extends RestaurantServiceGrpc.RestaurantServiceImplBase {
	private Jdbi db;
	private RestaurantManager manager;
	public RestaurantServiceImpl(Jdbi db) {
		this.db = db;
		this.manager = new RestaurantManager(db);
	}

	@Override
	public void createRestaurant(Restaurant request, StreamObserver<Restaurant> responseObserver) {
		int userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());

		try {
			int restId = manager.checkAndInsertRestaurant(request);

			// Insert current user as admin (owner) for new restaurant
			RoleManager rm = new RoleManager(db);
			int adminRoleId = rm.getRoleById("Admin");

			manager.addRestaurantRelationship(restId, userId, null, adminRoleId);

			Restaurant reply =
			Restaurant.newBuilder().setId(restId).build();
			responseObserver.onNext(reply);
			responseObserver.onCompleted();
		}
		catch (RestaurantManager.InvalidNameException | AddressManager.InvalidAddrNameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Name is invalid")));
		}
		catch (ContactManager.InvalidPhoneException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Phone can only include numeric characters")));
		}
		catch (ContactManager.InvalidEmailException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Invalid email")));
		}
		catch (AddressManager.InvalidLine1Exception e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Line1 cannot be empty")));
		}
		catch (AddressManager.InvalidLatException |
				AddressManager.InvalidLongException |
				AddressManager.InvalidCityException |
				AddressManager.InvalidStateException |
				AddressManager.InvalidZipException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Unable to extract location information from address")));
		}
		catch (RestaurantManager.InvalidCapFactorException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Capacity factor must be integer between 0 and 100")));
		}
		catch (RestaurantManager.InvalidRTimeException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Reservation time must be positive integer")));
		}
		catch (UnableToExecuteStatementException ex){
			// todo include StatusRuntimeException?
			ex.printStackTrace();
		}
		catch (UserManager.InvalidUsernameException e) {
			// should never occur
			responseObserver.onError(new StatusRuntimeException(Status.UNKNOWN.withDescription("Unknown user error")));
		}
	}

	@Override
	public void getRestaurant(Restaurant request, StreamObserver<Restaurant> responseObserver) {
		if (request.getId() == 0) {
			responseObserver.onError(new StatusRuntimeException(
					Status.DATA_LOSS.withDescription("Unknown restaurant error")));
			return;
		}

		Restaurant restaurant = manager.getRestaurant(request.getId());
		if (restaurant == null){
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unable to find requested restaurant")));
		}

		responseObserver.onNext(restaurant);
		responseObserver.onCompleted();
	}

	@Override
	public void setRestaurant(Restaurant request, StreamObserver<Restaurant> responseObserver) {
		try {
			manager.checkRestaurantPermission(request, null, false);
		}
		// should not occur
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
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to edit restaurant")));
			return;
		}

		try {
			responseObserver.onNext(manager.setRestaurant(request));
			responseObserver.onCompleted();
		}
		catch (RestaurantManager.InvalidNameException | AddressManager.InvalidAddrNameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Name is invalid")));
		}
		catch (ContactManager.InvalidPhoneException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Phone can only include numeric characters")));
		}
		catch (ContactManager.InvalidEmailException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Invalid email")));
		}
		catch (AddressManager.InvalidLine1Exception e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Line1 cannot be empty")));
		}
		catch (AddressManager.InvalidLatException |
				AddressManager.InvalidLongException |
				AddressManager.InvalidCityException |
				AddressManager.InvalidStateException |
				AddressManager.InvalidZipException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Unable to extract location information from address")));
		}
		catch (RestaurantManager.InvalidCapFactorException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Capacity factor must be integer between 0 and 100")));
		}
		catch (RestaurantManager.InvalidRTimeException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Reservation time must be positive integer")));
		}
		catch (ContactManager.InvalidContactIdException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unknown contact error")));
		}
		catch (AddressManager.InvalidAddressIdException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unknown address error")));
		}
	}

	@Override
	public void addRelationship(Relationship request, StreamObserver<RelationshipResponse> responseObserver) {
		if (!request.hasRestaurant() || request.getRestaurant().getId() == 0
			|| (request.getUser().getId() == 0 && request.getUser().getUsername().isBlank())) {
			responseObserver.onError(new StatusRuntimeException(
				Status.DATA_LOSS.withDescription("Unknown restaurant or user error")));
			return;
		}

		try {
			manager.checkRestaurantPermission(request.getRestaurant(), null, true);
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
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to add relationship")));
			return;
		}

		RoleManager rm = new RoleManager(db);
		String roleName = request.getRole().getValueDescriptor().getName();
		int roleId = rm.getRoleById(roleName);

		try {
			manager.addRestaurantRelationship(request.getRestaurant().getId(),
				request.getUser().getId(), request.getUser().getUsername(), roleId);
		}
		catch (UnableToExecuteStatementException ex) {
			ex.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
				Status.ALREADY_EXISTS.withDescription("Relationship already exists or invalid key")));
			return;
		}
		catch (UserManager.InvalidUsernameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("Invalid username")));
			return;
		}

		RelationshipResponse res = RelationshipResponse.newBuilder().build();
		responseObserver.onNext(res);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteRelationship(Relationship request, StreamObserver<RelationshipResponse> responseObserver) {
		if (!request.hasRestaurant() ||
			request.getRestaurant().getId() == 0 ||
			request.getUser().getId() == 0) {
			responseObserver.onError(new StatusRuntimeException(
					Status.DATA_LOSS.withDescription("Unknown restaurant or user error")));
			return;
		}

		try {
			manager.checkRestaurantPermission(request.getRestaurant(), null, true);
		}
		// should not occur
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
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to delete relationship")));
			return;
		}

		manager.deleteRestaurantRelationship(request.getRestaurant().getId(), request.getUser().getId());

		RelationshipResponse res = RelationshipResponse.newBuilder().build();
		responseObserver.onNext(res);
		responseObserver.onCompleted();
	}

	@Override
	public void createTable(CreateTableRequest request, StreamObserver<Table> responseObserver) {
		if (!request.hasTable() ||
			!request.hasTarget() ||
			request.getTable().getCapacity() <= 0 ||
			request.getTable().getLabel().isEmpty() ||
			request.getTarget().getId() == 0) {
			responseObserver.onError(new StatusRuntimeException(
						Status.DATA_LOSS.withDescription("Missing restaurant or table details")));
			return;
		}

		try {
			manager.checkRestaurantPermission(request.getTarget(), null, false);
		}
		// should not occur
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
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to add table at this restaurant")));
			return;
		}

		int tableId;
		try {
			tableId = manager.checkAndInsertTable(request.getTable(),
				request.getTarget());
		}
		catch (RestaurantManager.InvalidTableNameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
						Status.ALREADY_EXISTS.withDescription("Table label already exists"))
			);
			return;
		}
		catch (RestaurantManager.InvalidNameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Table label is invalid"))
			);
			return;
		}
		catch (RestaurantManager.InvalidCapacityException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.INVALID_ARGUMENT.withDescription("Table capacity must be greater than 0")));
			return;
		}
		responseObserver.onNext(this.manager.getTableById(tableId));
		responseObserver.onCompleted();
	}

	@Override
	public void getTableById(Table request, StreamObserver<Table> responseObserver) {
		if (request.getId() == 0){
			responseObserver.onError(new StatusRuntimeException(
					Status.DATA_LOSS.withDescription("Unknown table error")));
			return;
		}

		try {
			manager.checkRestaurantPermission(null, request, false);
		}
		catch (RestaurantManager.InvalidTableIdException |
				RestaurantManager.InvalidRestException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unknown table error")));
			return;
		}
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to view table")));
			return;
		}

		// Table has been already checked for in checkRestaurantPermission
		Table table = this.manager.getTableById(request.getId());

		responseObserver.onNext(table);
		responseObserver.onCompleted();
	}

	@Override
	public void searchByCategory(Category request, StreamObserver<Restaurant> responseObserver) {
		List<Restaurant> results = manager.searchByCategory(request);
		for (Restaurant result: results) {
			responseObserver.onNext(result);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getUsersByRestaurant(Restaurant request, StreamObserver<Relationship> responseObserver) {
		List<Relationship> results = manager.getRelationshipByRestaurant(request);
		for (Relationship result: results) {
			responseObserver.onNext(result);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getRestaurantsByUser(User request, StreamObserver<Relationship> responseObserver) {
		List<Relationship> results = manager.getRelationshipByUser(request);
		for (Relationship result: results) {
			responseObserver.onNext(result);
		}
		responseObserver.onCompleted();
	}

	@Override
	public void getTablesByRestaurant(Restaurant request, StreamObserver<Table> responseObserver) {
		if (request.getId() == 0) {
			responseObserver.onError(
				new StatusRuntimeException(Status.DATA_LOSS.withDescription("Unknown restaurant error")));
			return;
		}

		try {
			manager.checkRestaurantPermission(request, null, false);
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
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to view tables for this restaurant")));
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
		Restaurant restaurant;
		try {
			restaurant = manager.checkRestaurantPermission(null, request, false);
		}
		catch (RestaurantManager.InvalidTableIdException |
				RestaurantManager.InvalidRestException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unknown table error")));
			return;
		}
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Unauthorized to edit this table")));
			return;
		}

		Table retTable;
		try {
			retTable = manager.setTable(request, restaurant);
		}
		catch (RestaurantManager.InvalidTableNameException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.ALREADY_EXISTS.withDescription("Table name already exists for this restaurant")));
			return;
		}

		responseObserver.onNext(retTable);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteTable(Table request, StreamObserver<DeleteTableResponse> responseObserver) {
		try {
			manager.checkRestaurantPermission(null, request, false);
		}
		catch (RestaurantManager.InvalidTableIdException |
				RestaurantManager.InvalidRestException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.NOT_FOUND.withDescription("Unknown table error")));
			return;
		}
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to edit tables")));
			return;
		}

		DeleteTableResponse deleteTableResponse = DeleteTableResponse
			.newBuilder().build();

		manager.deleteTableById(request.getId());

		responseObserver.onNext(deleteTableResponse);
		responseObserver.onCompleted();
	}

	@Override
	public void deleteRestaurant(Restaurant restaurant, StreamObserver<DeleteRestaurantResponse> responseObserver){
		//Deleting a restaurant should be a privileged operation
		try {
			manager.checkRestaurantPermission(restaurant, null, true);
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
			responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("An error occurred")));
			return;
		}
		catch (RestaurantManager.RestUnauthorizedException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(
					Status.PERMISSION_DENIED.withDescription("Not authorized to edit restaurant")));
			return;
		}

		DeleteRestaurantResponse deleteRestaurantResponse = DeleteRestaurantResponse
				.newBuilder().build();

		try {
			manager.deleteRestaurant(restaurant.getId());
		}
		catch (AddressManager.InvalidAddressIdException | ContactManager.InvalidContactIdException e) {
			e.printStackTrace();
			responseObserver.onError(new StatusRuntimeException(Status.NOT_FOUND.withDescription("An error occurred")));
			return;
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

	@Override
	public void searchRestaurants(RestaurantSearchRequest request, StreamObserver<RestaurantSearchResponse> responseObserver) {
		List<RestaurantSearchResponse> restaurants = manager.searchRestaurants(request);

		for (RestaurantSearchResponse restaurant: restaurants) {
			responseObserver.onNext(restaurant);
		}
		responseObserver.onCompleted();
	}
}

package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class RestaurantManager {
	private Jdbi db;
	public RestaurantManager(Jdbi db) {
		this.db = db;
	}

	public int checkAndInsertRestaurant(Restaurant restaurant)
			throws InvalidRestNameException,
			ContactManager.InvalidContactIdException,
			ContactManager.InvalidPhoneException,
			ContactManager.InvalidEmailException,
			AddressManager.InvalidAddrFormException,
			AddressManager.InvalidLatException,
			AddressManager.InvalidLongException,
			AddressManager.InvalidAddrNameException,
			AddressManager.InvalidZipException {
		// TODO restaurant checks on category (and capacity factor and rTime?)
		checkName(restaurant.getName());

		ContactManager cm = new ContactManager(db);
		int contId = cm.checkAndInsertContact(restaurant.getContact());
		AddressManager am = new AddressManager(db);
		int addrId = am.checkAndInsertAddress(restaurant.getAddress());

		return db.withExtension(RestaurantDao.class, dao -> {
			return dao.insertRestaurant(addrId, contId, restaurant);
		});
	}

	public void addRestaurantRelationship(int restId, int userId,
	                                      int adminRoleId) {
		db.useExtension(RestaurantDao.class, dao -> {
			dao.addRestaurantRelationship(restId, userId,
			                              adminRoleId);
		});
	}

	public Restaurant getRestaurant(int id) {
		return db.withExtension(RestaurantDao.class, dao -> {
			return dao.getRestaurant(id);
		});
	}

	public List<Restaurant> searchByCategory(Category cat) {
		return db.withExtension(RestaurantDao.class, dao -> {
			return dao.searchByCategory(cat.getCategory());
		});
	}

	public List<Relationship> getRelationshipByRestaurant(Restaurant res) {
		return db.withExtension(RestaurantDao.class, dao -> {
			return dao.getRelationshipByRestaurant(res.getId());
		});
	}

	public List<Relationship> getRelationshipByUser(User user) {
		return db.withExtension(RestaurantDao.class, dao -> {
			return dao.getRelationshipByUser(user.getId());
		});
	}

	public List<Table> getRestaurantTables(int restaurant_id){
		return db.withExtension(RestaurantDao.class, dao ->
			dao.getRestaurantTables(restaurant_id));
	}

	public Restaurant setRestaurant(Restaurant target)
			throws InvalidRestNameException,
			ContactManager.InvalidContactIdException,
			ContactManager.InvalidPhoneException,
			ContactManager.InvalidEmailException,
			AddressManager.InvalidAddressIdException,
			AddressManager.InvalidAddrFormException,
			AddressManager.InvalidLatException,
			AddressManager.InvalidLongException,
			AddressManager.InvalidAddrNameException,
			AddressManager.InvalidZipException {
		// TODO checks for category (and capacity factor and rTime?)
		checkName(target.getName());

		ContactManager cm = new ContactManager(db);
		cm.setContact(target.getContact());
		AddressManager am = new AddressManager(db);
		am.setAddress(target.getAddress());

		return db.withExtension(RestaurantDao.class, dao -> {
			dao.setRestaurant(target);
			return dao.getRestaurant(target.getId());
		});
	}

	public boolean canEditRestaurant(int userId, int restaurantId, boolean privileged){
		Optional<String> role = db.withExtension(
			RestaurantDao.class,
			dao -> dao.getRestaurantUserRole(userId, restaurantId)
		);
		if(privileged)
			return role.isPresent() && role.get().equals("Admin");
		return role.isPresent() && (role.get().equals("Admin") || role.get().equals("Manager"));
	}

	public int checkAndInsertTable(Table table, Restaurant restaurant)
		throws InvalidTableException {
		//Todo: maybe validate table name?
		Optional<Table> existingTable = db.withExtension(
			RestaurantDao.class,
			dao -> dao.getTableByName(table.getLabel(),
			                          restaurant.getId())
		);
		if(existingTable.isPresent()){
			throw new InvalidTableException("Table name already exists" +
				" for given restaurant");
		}

		return db.withExtension(RestaurantDao.class,
			dao -> dao.insertTable(table, restaurant));
	}

	public Optional<Restaurant> getRestaurantByTable(int tableId){
		return db.withExtension(RestaurantDao.class,
			dao -> dao.getRestaurantByTable(tableId));
	}

	public Table getTableById(int tableId) {
		return db.withExtension(RestaurantDao.class,
				dao -> dao.getTableById(tableId));
	}

	public Table setTable(Table table, Restaurant restaurant)
		throws InvalidTableException {
		Optional<Table> existingTable = db.withExtension(
			RestaurantDao.class,
			dao -> dao.getTableByName(table.getLabel(),
			                          restaurant.getId())
		);
		if(existingTable.isPresent()
			&& existingTable.get().getId() != table.getId()){
			throw new InvalidTableException("Table name already " +
			                                "exists for given " +
			                                "restaurant");
		}

		db.useExtension(RestaurantDao.class,
		                dao -> dao.setTable(table));
		return getTableById(table.getId());
	}

	public void deleteTableById(int table_id){
		db.useExtension(RestaurantDao.class,
		                dao -> dao.deleteTableById(table_id));
	}

	public static class InvalidTableException extends Exception {
		public InvalidTableException(String message) {
			super(message);
		}
	}

	public void deleteRestaurant(int rest_id) throws AddressManager.InvalidAddressIdException, ContactManager.InvalidContactIdException {
		ContactManager cm = new ContactManager(db);
		AddressManager am = new AddressManager(db);

		Restaurant rest = getRestaurant(rest_id);

		//Delete Users
		List<Relationship> restUsers = db.withExtension(RestaurantDao.class, dao -> {
			return dao.getRelationshipByRestaurant(rest_id);
		});

		for (Relationship rel: restUsers) {
			db.useExtension(RestaurantDao.class, dao -> {
				dao.deleteRelationship(rel.getUser().getId(), rest_id);
			});
		}

		//Delete Tables
		List<Table> restTables = db.withExtension(RestaurantDao.class, dao -> {
			return dao.getRestaurantTables(rest_id);
		});
		for(Table table: restTables){
			deleteTableById(table.getId());
		}

		db.useExtension(RestaurantDao.class, dao -> {
			dao.deleteRestaurant(rest_id);
		});

		cm.deleteContact(rest.getContact().getId());
		am.deleteAddress(rest.getAddress().getId());
	}

	public void checkName(String name) throws InvalidRestNameException {
		if (!name.matches("^[a-zA-Z0-9]*$")) {
			throw new InvalidRestNameException("Name must only include alphanumeric characters.");
		}
	}

	public static class InvalidRestNameException extends Exception {
		public InvalidRestNameException(String message) {
			super(message);
		}
	}
}

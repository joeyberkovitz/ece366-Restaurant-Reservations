package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Restaurant;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantDao;
import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Category;
import edu.cooper.ece366.restaurantReservation.grpc.Table;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;

public class RestaurantManager {
	private Jdbi db;
	public RestaurantManager(Jdbi db) {
		this.db = db;
	}

	public int checkAndInsertRestaurant(Restaurant restaurant)
	throws ContactManager.InvalidContactIdException,
	       ContactManager.InvalidPhoneException,
	       ContactManager.InvalidEmailException,
	       AddressManager.InvalidAddressIdException {
		// TODO restaurant checks
		ContactManager cm = new ContactManager(db);
		int contId = cm.checkAndInsertContact(restaurant.getContact());
		AddressManager am = new AddressManager(db);
		int addrId = am.checkAndInsertAddress(restaurant.getAddress());

		return db.withExtension(RestaurantDao.class, dao -> {
			return dao.insertRestaurant(addrId, contId, restaurant);
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

	public Restaurant setRestaurant(Restaurant target)
	throws ContactManager.InvalidContactIdException,
	       ContactManager.InvalidPhoneException,
	       ContactManager.InvalidEmailException,
	       AddressManager.InvalidAddressIdException {
		int contId;
		int addrId;
		// TODO support changes to name only
		//if(target.getContact() != null) {
			ContactManager cm = new ContactManager(db);
			contId = cm.setContact(target.getContact()).getId();
		//}
		//if(target.getAddress() != null) {
			AddressManager am = new AddressManager(db);
			addrId = am.setAddress(target.getAddress()).getId();
		//}

		return db.withExtension(RestaurantDao.class, dao -> {
			dao.setRestaurant(addrId, contId, target);
			return dao.getRestaurant(target.getId());
		});
	}

	public boolean canEditRestaurant(int userId, int restaurantId, boolean privileged){
		Optional<String> role = db.withExtension(RestaurantDao.class, d ->
				d.getRestaurantUserRole(userId, restaurantId));
		if(privileged)
			return role.isPresent() && role.get().equals("Admin");
		return role.isPresent() && (role.get().equals("Admin") || role.get().equals("Manager"));
	}

	public int checkAndInsertTable(Table table, Restaurant restaurant)
		throws InvalidTableException {
		//Todo: maybe validate table name?
		Optional<Table> existingTable = db.withExtension(RestaurantDao.class,
			d -> d.getTableByName(table.getLabel(), restaurant.getId()));
		if(existingTable.isPresent()){
			throw new InvalidTableException("Table name already exists" +
				" for given restaurant");
		}

		return db.withExtension(RestaurantDao.class,
			d -> d.insertTable(table, restaurant));
	}

	public static class InvalidTableException extends Exception {
		public InvalidTableException(String message) {
			super(message);
		}
	}
}

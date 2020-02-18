package edu.cooper.ece366.restaurantReservation.grpc.Restaurants;

import edu.cooper.ece366.restaurantReservation.grpc.Restaurant;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantDao;
import edu.cooper.ece366.restaurantReservation.grpc.Addresses.AddressManager;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Category;
import org.jdbi.v3.core.Jdbi;

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
}

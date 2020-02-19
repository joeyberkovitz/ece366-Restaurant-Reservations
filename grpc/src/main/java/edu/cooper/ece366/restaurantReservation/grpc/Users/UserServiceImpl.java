package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantManager;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.UserServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private Jdbi db;
    private UserManager manager;

    public UserServiceImpl(Jdbi db){
        this.db = db;
        this.manager = new UserManager(db);
    }

    @Override
    public void getUser(User req, StreamObserver<User> responseObserver) {
        responseObserver.onNext(manager.getUser(req.getId()));
        responseObserver.onCompleted();
    }

    @Override
    public void setUser(User req, StreamObserver<User> responseObserver) {
        try {
            responseObserver.onNext(manager.setUser(req));
            responseObserver.onCompleted();
        }
        catch (ContactManager.InvalidContactIdException |
                ContactManager.InvalidPhoneException |
                ContactManager.InvalidEmailException |
                UserManager.InvalidNameException |
                UserManager.InvalidUserException e) {
            e.printStackTrace();
        }
    }
}

package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.UserServiceGrpc;
import org.jdbi.v3.core.Jdbi;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private Jdbi db;

    public UserServiceImpl(Jdbi db) {
        this.db = db;
    }

    @Override
    public User getUser(User user) {
        UserManager um = new UserManager(db);

        return um.getUser(user.getId());
    }
}

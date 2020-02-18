package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.UserServiceGrpc;
import org.jdbi.v3.core.Jdbi;

public class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {

    private Jdbi db;

    public UserServiceImpl(Jdbi db){
        this.db = db;
    }

    @Override
    public User SetUser(User user) {
        UserManager um = new UserManager(db);
        if (user.getId() == 0) {
            //todo throw error
        }
        else {

        }
    }
}

package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.UserServiceGrpc;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
        User user = manager.getUser(req.getId());
        if(user == null){
            throw new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription("Unable to find requested user"));
        }

        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }

    @Override
    public void setUser(User req, StreamObserver<User> responseObserver) {
        //todo permissions
        try {
            responseObserver.onNext(manager.setUser(req));
            responseObserver.onCompleted();
        }
        catch (ContactManager.InvalidContactIdException | ContactManager.InvalidPhoneException | ContactManager.InvalidEmailException | UserManager.InvalidNameException | UserManager.NoIdException | UserManager.InvalidUsernameException e) {
            e.printStackTrace();
        }
    }
}

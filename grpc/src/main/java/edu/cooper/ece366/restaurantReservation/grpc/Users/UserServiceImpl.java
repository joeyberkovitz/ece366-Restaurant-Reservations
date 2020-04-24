package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
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
        int userId = req.getId();
        if (userId == 0)
            userId = Integer.parseInt(AuthInterceptor.CURRENT_USER.get());

        User user = manager.getUser(userId);
        if(user == null){
            throw new StatusRuntimeException(Status.NOT_FOUND
                    .withDescription("Unable to find requested user"));
        }

        responseObserver.onNext(user);
        responseObserver.onCompleted();
    }

    @Override
    public void setUser(User req, StreamObserver<User> responseObserver) {
        if (req.getId() != Integer.parseInt(AuthInterceptor.CURRENT_USER.get())) {
            // todo add permissions for admin
            throw new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("Not authorized to change this " +
                    "user"));
        }

        try {
            responseObserver.onNext(manager.setUser(req));
            responseObserver.onCompleted();
        }
        catch (UserManager.NoIdException |
                UserManager.InvalidUsernameException e) {
            e.printStackTrace();
            throw new StatusRuntimeException(Status.NOT_FOUND.withDescription("Unknown user error"));
        }
        catch (ContactManager.InvalidContactIdException e) {
            e.printStackTrace();
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Unknown contact error"));
        }
        catch (UserManager.InvalidNameException e) {
            e.printStackTrace();
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Name can only include alphabet " +
                    "characters"));
        }
        catch (ContactManager.InvalidPhoneException e) {
            e.printStackTrace();
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Phone can only include numeric " +
                    "characters"));
        }
        catch (ContactManager.InvalidEmailException e) {
            e.printStackTrace();
            throw new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid email"));
        }
    }
}

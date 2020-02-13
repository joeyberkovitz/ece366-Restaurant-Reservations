package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserDao;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private Jdbi db;

    public AuthServiceImpl(Jdbi db){
        this.db = db;
    }

    @Override
    public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver){

        User tempUser = User.newBuilder().mergeFrom(request.getUser()).setPoints(0).build();

        UserManager um = new UserManager(db);
        int contId = 0;
        try {
            contId = um.checkUser(tempUser);
        } catch (UserManager.InvalidUsernameException | UserManager.InvalidNameException | ContactManager.InvalidContactIdException | ContactManager.InvalidPhoneException | ContactManager.InvalidEmailException e) {
            e.printStackTrace();
            //Todo: On error, return it to gRPC
            return;
        }

        Contact newContact = Contact.newBuilder().setId(contId).build();
        tempUser = User.newBuilder().mergeFrom(tempUser).setContact(newContact).build();

        int roleId = db.withExtension(RoleDao.class, dao -> {
            return dao.getRoleIdByName("Customer");
        });

        //Todo: hash pw
        CreateUserRequest tempRequest = CreateUserRequest.newBuilder().mergeFrom(request).setUser(tempUser).build();
        int userId = db.withExtension(UserDao.class, dao -> {
            return dao.insertUserDriver(tempRequest, roleId);
        });

        User reply =
                User.newBuilder().setId(userId).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

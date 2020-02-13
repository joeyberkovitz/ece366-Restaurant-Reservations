package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import edu.cooper.ece366.restaurantReservation.grpc.AuthServiceGrpc;
import edu.cooper.ece366.restaurantReservation.grpc.Contact.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.RestaurantServiceOuterClass;
import edu.cooper.ece366.restaurantReservation.grpc.RoleDao;
import edu.cooper.ece366.restaurantReservation.grpc.User.UserDao;
import edu.cooper.ece366.restaurantReservation.grpc.User.UserManager;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
    private Jdbi db;

    public AuthServiceImpl(Jdbi db){
        this.db = db;
    }

    @Override
    public void createUser(RestaurantServiceOuterClass.CreateUserRequest request, StreamObserver<RestaurantServiceOuterClass.User> responseObserver) throws UserManager.InvalidUsernameException, UserManager.InvalidNameException, ContactManager.InvalidEmailException, ContactManager.InvalidPhoneException, ContactManager.InvalidContactIdException {

        RestaurantServiceOuterClass.User tempUser = RestaurantServiceOuterClass.User.newBuilder().mergeFrom(request.getUser()).setPoints(0).build();

        UserManager um = new UserManager(db);
        int contId = um.checkUser(tempUser);

        RestaurantServiceOuterClass.Contact newContact = RestaurantServiceOuterClass.Contact.newBuilder().setId(contId).build();
        tempUser = RestaurantServiceOuterClass.User.newBuilder().mergeFrom(tempUser).setContact(newContact).build();

        int roleId = db.withExtension(RoleDao.class, dao -> {
            return dao.getRoleIdByName("CUSTOMER");
        });

        RestaurantServiceOuterClass.CreateUserRequest tempRequest = RestaurantServiceOuterClass.CreateUserRequest.newBuilder().mergeFrom(request).setUser(tempUser).build();
        int userId = db.withExtension(UserDao.class, dao -> {
            return dao.insertUserDriver(tempRequest, roleId);
        });

        RestaurantServiceOuterClass.User reply =
                RestaurantServiceOuterClass.User.newBuilder().mergeFrom(tempRequest).setId(userId).build();
        responseObserver.onNext(reply);
        responseObserver.onCompleted();
    }
}

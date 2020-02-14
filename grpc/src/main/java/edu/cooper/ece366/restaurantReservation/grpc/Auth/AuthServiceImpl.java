package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserDao;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

import java.util.Optional;
import java.util.Properties;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
	private Jdbi db;
	private Properties prop;
	private Argon2 argon2;
	private int argonIter, argonMem, argonPar;

	public AuthServiceImpl(Jdbi db, Properties prop){
		this.db = db;
		this.prop = prop;
		initHash();
	}

	private void initHash(){
		argon2 = Argon2Factory.create();
		argonIter = Integer.parseInt(prop.getProperty("argon.iter"));
		argonMem = Integer.parseInt(prop.getProperty("argon.mem"));
		argonPar = Integer.parseInt(prop.getProperty("argon.par"));
	}

	@Override
	public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver){

		User tempUser = User.newBuilder().mergeFrom(request.getUser()).setPoints(0).build();

		UserManager um = new UserManager(db);
		int contId = 0;
		try {
			contId = um.checkAndInsertUser(tempUser);
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

		CreateUserRequest tempRequest = CreateUserRequest.newBuilder()
				.setPassword(hashPassword(request.getPassword()))
				.setUser(tempUser)
				.build();
		int userId = db.withExtension(UserDao.class, dao -> {
			return dao.insertUserDriver(tempRequest, roleId);
		});

		User reply =
				User.newBuilder().setId(userId).build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}

	@Override
	public void authenticate(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
		AuthResponse.Builder resBuild = AuthResponse.newBuilder();
		if(request.getPassword().isEmpty()){
			//Todo: return exception to gRPC
			throw new RuntimeException("Password can't be blank");
		}

		Optional<DBHashResponse> dbHash = db.withExtension(UserDao.class,
				d -> d.getUserHash(request.getUsername()));

		//If username is invalid, hash the password anyway to waste time and
		// make it non-obvious that username is invalid
		if(dbHash.isEmpty()) {
			hashPassword(request.getPassword());
			throw new RuntimeException("Username or Password is invalid");
		}

		if(verifyPassword(request.getPassword(), dbHash.get().getPassword())){
			//Todo: generate auth and refresh tokens
			resBuild.setAuthToken("Auth Token");
			resBuild.setRefreshToken("RefreshToken");

			System.out.println("User id is: " + dbHash.get().getId());
		}
		else {
			throw new RuntimeException("Username or Password is invalid");
		}
		responseObserver.onNext(resBuild.build());
		responseObserver.onCompleted();
	}

	private String hashPassword(String password){
		return argon2.hash(argonIter, argonMem, argonPar, password.toCharArray());
	}

	private boolean verifyPassword(String password, String hash){
		return argon2.verify(hash, password.toCharArray());
	}
}

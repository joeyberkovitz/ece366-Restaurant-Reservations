package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserDao;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.Properties;

public class AuthServiceImpl extends AuthServiceGrpc.AuthServiceImplBase {
	private Jdbi db;
	private Properties prop;
	private Argon2 argon2;
	private int argonIter, argonMem, argonPar;
	private AuthManager authManager;

	public AuthServiceImpl(Jdbi db, Properties prop){
		this.db = db;
		this.prop = prop;
		this.authManager = new AuthManager(prop, db);
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

		UserManager um = new UserManager(db);
		final int contId;
		try {
			um.checkUsername(request.getUser().getUsername());
			um.checkName(request.getUser().getFname(),"First");
			um.checkName(request.getUser().getLname(),"Last");

			ContactManager cm = new ContactManager(db);
			contId = cm.checkAndInsertContact(request.getUser().getContact());
		} catch (UserManager.InvalidUsernameException | UserManager.InvalidNameException | ContactManager.InvalidContactIdException | ContactManager.InvalidPhoneException | ContactManager.InvalidEmailException e) {
			e.printStackTrace();
			//Todo: On error, return it to gRPC
			return;
		}

		int roleId = db.withExtension(RoleDao.class, dao -> {
			return dao.getRoleIdByName("Customer");
		});

		int userId = db.withExtension(UserDao.class, dao -> {
			return dao.insertUser(hashPassword(request.getPassword()), contId, request.getUser(), roleId);
		});

		User reply = User.newBuilder().setId(userId).build();
		responseObserver.onNext(reply);
		responseObserver.onCompleted();
	}

	@Override
	public void authenticate(AuthRequest request, StreamObserver<AuthResponse> responseObserver) {
		AuthResponse.Builder resBuild = AuthResponse.newBuilder();
		if(request.getPassword().isEmpty()){
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT
				.withDescription("Password can't be blank"));
		}

		Optional<DBHashResponse> dbHash = db.withExtension(UserDao.class,
				dao -> dao.getUserHash(request.getUsername()));

		//If username is invalid, hash the password anyway to waste time and
		// make it non-obvious that username is invalid
		if(dbHash.isEmpty()) {
			hashPassword(request.getPassword());
			throw new StatusRuntimeException(Status.UNAUTHENTICATED
				.withDescription("Username or Password is invalid"));
		}

		if(verifyPassword(request.getPassword(), dbHash.get().getPassword())){
			List<String> tokens = this.authManager.genTokens(dbHash.get().getId(), request.getUserAgent());
			resBuild.setAuthToken(tokens.get(0));
			resBuild.setRefreshToken(tokens.get(1));
		}
		else {
			throw new StatusRuntimeException(Status.UNAUTHENTICATED
				.withDescription("Username or Password is invalid"));
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

	@Override
	public void refreshToken(RefreshRequest request, StreamObserver<AuthResponse> responseObserver) {
		//Todo: maybe validate retrieved user id against id in token
		this.authManager.validateToken(request.getRefreshToken());
		//DAO verifies that refresh token isn't revoked
		Optional<Integer> userId = db.withExtension(UserDao.class,
			dao -> dao.checkRefreshToken(request.getRefreshToken()));
		if(userId.isEmpty())
			throw new StatusRuntimeException(Status.PERMISSION_DENIED
				.withDescription("Invalid or revoked refresh token"));

		List<String> tokens = authManager.genTokens(userId.get(), request.getUserAgent());
		AuthResponse res = AuthResponse.newBuilder()
				.setAuthToken(tokens.get(0))
				.setRefreshToken(tokens.get(1))
				.build();

		responseObserver.onNext(res);
		responseObserver.onCompleted();
	}
}

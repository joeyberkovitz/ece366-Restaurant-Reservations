package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import edu.cooper.ece366.restaurantReservation.grpc.*;
import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
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
	private AuthManager manager;

	public AuthServiceImpl(Jdbi db, Properties prop){
		this.db = db;
		this.prop = prop;
		this.manager = new AuthManager(db);
		initHash();
	}

	private void initHash(){
		argon2 = Argon2Factory.create();
		argonIter = Integer.parseInt(prop.getProperty("argon.iter"));
		argonMem = Integer.parseInt(prop.getProperty("argon.mem"));
		argonPar = Integer.parseInt(prop.getProperty("argon.par"));
	}

	@Override
	public void createUser(CreateUserRequest request, StreamObserver<User> responseObserver) {
		int userId;
		try {
			userId = this.manager.createUser(request.getUser(),
				hashPassword(request.getPassword()));
		} catch (UserManager.InvalidUsernameException |
		         UserManager.InvalidNameException |
		         ContactManager.InvalidContactIdException |
		         ContactManager.InvalidPhoneException |
		         ContactManager.InvalidEmailException e) {
			e.printStackTrace();
			throw new StatusRuntimeException(Status.INVALID_ARGUMENT
				.withDescription(e.getMessage()));
		}

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

		Optional<DBHashResponse> dbHash = manager.getUserHash(request.getUsername());

		// If username is invalid, hash the password anyway to waste time and
		// make it non-obvious that username is invalid
		if(dbHash.isEmpty()) {
			hashPassword(request.getPassword());
			throw new StatusRuntimeException(Status.UNAUTHENTICATED
				.withDescription("Username or Password is invalid"));
		}

		if(verifyPassword(request.getPassword(), dbHash.get().getPassword())){
			List<String> tokens = this.manager.genTokens(dbHash.get().getId(), request.getUserAgent());
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
		int tokenUserId = Integer.parseInt(
			AuthManager.validateToken(request.getRefreshToken()));
		//DAO verifies that refresh token isn't revoked
		Optional<Integer> userId = this.manager.checkRefreshToken(request.getRefreshToken());

		if(userId.isEmpty() || userId.get() != tokenUserId)
			throw new StatusRuntimeException(Status.PERMISSION_DENIED
				.withDescription("Invalid or revoked refresh token"));

		List<String> tokens = manager.genTokens(userId.get(), request.getUserAgent());
		AuthResponse res = AuthResponse.newBuilder()
				.setAuthToken(tokens.get(0))
				.setRefreshToken(tokens.get(1))
				.build();

		responseObserver.onNext(res);
		responseObserver.onCompleted();
	}
}

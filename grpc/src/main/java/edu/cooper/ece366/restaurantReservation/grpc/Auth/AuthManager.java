package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import edu.cooper.ece366.restaurantReservation.grpc.Contacts.ContactManager;
import edu.cooper.ece366.restaurantReservation.grpc.Role.RoleManager;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserDao;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserManager;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.jdbi.v3.core.Jdbi;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.*;

public class AuthManager {
	//private Properties prop;
	private Jdbi db;

	private static String privKeyName = "privKey.pem";
	private static String pubKeyName = "pubKey.pem";

	public AuthManager(Jdbi db){
		this.db = db;
	}

	private static String readFile(String filename){
		InputStream inputStream = AuthManager.class.getClassLoader()
			.getResourceAsStream(filename);

		try {
			assert inputStream != null;
			return new String(inputStream.readAllBytes());
		} catch (IOException e) {
			throw new RuntimeException("Unable to load file: " + filename);
		}
	}

	private static PrivateKey loadPrivKey(){
		String privKey = readFile(privKeyName);

		byte[] privateKey = Decoders.BASE64.decode(privKey);
		KeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
		KeyFactory keyFactory = null;
		PrivateKey privateKey1;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			privateKey1 = keyFactory.generatePrivate(keySpec);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
			e.printStackTrace();
			throw new RuntimeException("Error getting pkey");
		}
		return privateKey1;
	}

	private static PublicKey loadPubKey(){
		String pubKey = readFile(pubKeyName);
		byte[] pubKeyBytes = Decoders.BASE64.decode(pubKey);
		KeySpec keySpec = new X509EncodedKeySpec(pubKeyBytes);
		KeyFactory keyFactory;
		PublicKey publicKey;
		try{
			keyFactory = KeyFactory.getInstance("RSA");
			publicKey = keyFactory.generatePublic(keySpec);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			e.printStackTrace();
			throw new RuntimeException("Error getting pubkey");
		}
		return publicKey;
	}

	public List<String> genTokens(int userID, String userAgent){
		PrivateKey privateKey = loadPrivKey();

		User user = db.withExtension(UserDao.class, d->d.getUser(userID));

		Date issueDate = new Date();
		Date expDate = Date.from(issueDate.toInstant().plus(Duration.ofHours(1)));
		Date refreshExp = Date.from(issueDate.toInstant().plus(Duration.ofDays(1)));
		Date notBefore = Date.from(issueDate.toInstant().minus(Duration.ofMinutes(5)));
		String authToken = Jwts.builder()
				.setExpiration(expDate)
				.setIssuedAt(issueDate)
				.setNotBefore(notBefore)
				.setSubject(Integer.toString(userID))
				.signWith(privateKey, SignatureAlgorithm.RS256)
				.compact();

		String refreshToken = Jwts.builder()
				.setExpiration(refreshExp)
				.setIssuedAt(issueDate)
				.setNotBefore(notBefore)
				.setSubject(Integer.toString(userID))
				.signWith(privateKey, SignatureAlgorithm.RS256)
				.compact();

		db.useExtension(UserDao.class, dao ->
				dao.insertUserToken(userID, refreshToken, userAgent, refreshExp));

		ArrayList<String> res = new ArrayList<String>();
		res.add(authToken);
		res.add(refreshToken);
		return res;
	}

	public static String validateToken(String authToken){
		Jws<Claims> jws;
		try{
			jws = Jwts.parserBuilder().setSigningKey(loadPubKey()).build()
					.parseClaimsJws(authToken);
			return jws.getBody().getSubject();
		}
		catch (JwtException e){
			throw new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("Invalid token"));
		}
	}

	public Optional<DBHashResponse> getUserHash(String username){
		return db.withExtension(UserDao.class,
				dao -> dao.getUserHash(username));
	}

	public Optional<Integer> checkRefreshToken(String refreshToken) {
		return db.withExtension(UserDao.class,
				dao -> dao.checkRefreshToken(refreshToken));
	}

	public int createUser(User user, String password)
	throws UserManager.InvalidUsernameException,
	       UserManager.InvalidNameException,
	       ContactManager.InvalidContactIdException,
	       ContactManager.InvalidPhoneException,
	       ContactManager.InvalidEmailException
	{
		final int contId;
		UserManager um = new UserManager(db);
		um.checkUsername(user.getUsername());
		um.checkName(user.getFname(),"First");
		um.checkName(user.getLname(),"Last");

		ContactManager cm = new ContactManager(db);
		contId = cm.checkAndInsertContact(user.getContact());

		RoleManager rm = new RoleManager(db);
		int roleId = rm.getRoleById("Customer");

		return db.withExtension(UserDao.class, dao -> {
			return dao.insertUser(password, contId, user, roleId);
		});
	}
}

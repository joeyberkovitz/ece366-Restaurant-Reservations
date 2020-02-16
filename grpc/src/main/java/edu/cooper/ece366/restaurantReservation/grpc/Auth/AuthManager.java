package edu.cooper.ece366.restaurantReservation.grpc.Auth;

import edu.cooper.ece366.restaurantReservation.grpc.User;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserDao;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import org.jdbi.v3.core.Jdbi;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

public class AuthManager {
	private Properties prop;
	private Jdbi db;

	public AuthManager(Properties properties, Jdbi db){
		this.prop = properties;
		this.db = db;
		//Todo: get keys from pem and key files
	}

	private PrivateKey loadPrivKey(){
		String privKey = prop.getProperty("auth.sKey");
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

	private PublicKey loadPubKey(){
		String pubKey = prop.getProperty("auth.pKey");
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

		db.useExtension(UserDao.class, d ->
				d.insertUserToken(userID, refreshToken, userAgent, refreshExp));

		ArrayList<String> res = new ArrayList<String>();
		res.add(authToken);
		res.add(refreshToken);
		return res;
	}

	public String validateToken(String authToken){
		Jws<Claims> jws;
		try{
			jws = Jwts.parserBuilder().setSigningKey(loadPubKey()).build()
					.parseClaimsJws(authToken);
			return jws.getBody().getSubject();
		}
		catch (JwtException e){
			throw new RuntimeException("Invalid token");
		}
	}
}

package edu.cooper.ece366.restaurantReservation.grpc;

import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import de.mkammerer.argon2.Argon2Helper;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.util.Properties;

public class ComputeArgonParams {

	public static void main(String[] args) {
		Argon2 argon2 = Argon2Factory.create();
		int iterations = Argon2Helper.findIterations(argon2, 1000, 65536, 1);

		System.out.println("Optimal number of iterations: " + iterations);

		KeyPair keyPair = Keys.keyPairFor(SignatureAlgorithm.RS256);
		String secretString = Encoders.BASE64.encode(keyPair.getPrivate().getEncoded());
		String pubString = Encoders.BASE64.encode(keyPair.getPublic().getEncoded());
		System.out.println("Secret key: " + secretString);
		System.out.println("Public key: " + pubString);
	}
}

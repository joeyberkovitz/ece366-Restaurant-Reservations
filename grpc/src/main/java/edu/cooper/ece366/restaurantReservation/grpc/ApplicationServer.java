package edu.cooper.ece366.restaurantReservation.grpc;

import com.mysql.cj.jdbc.MysqlDataSource;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthInterceptor;
import edu.cooper.ece366.restaurantReservation.grpc.Auth.AuthServiceImpl;
import edu.cooper.ece366.restaurantReservation.grpc.Restaurants.RestaurantServiceImpl;
import edu.cooper.ece366.restaurantReservation.grpc.Users.UserServiceImpl;
import edu.cooper.ece366.restaurantReservation.grpc.ProtoBeanMapper;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;
import io.grpc.stub.StreamObserver;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static io.grpc.ServerInterceptors.intercept;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class ApplicationServer {
	private static final Logger logger = Logger.getLogger(ApplicationServer.class.getName());

	private Server server;
	private static Jdbi jdbi;
	Properties prop = new Properties();
	String propFileName = "config.properties";

	private void start() throws IOException {
		InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);
		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName + "' not found in the classpath");
		}

		MysqlDataSource ds = new MysqlDataSource();
		ds.setUrl(prop.getProperty("db.connString"));
		ds.setUser(prop.getProperty("db.user"));
		ds.setPassword(prop.getProperty("db.pass"));
		jdbi = Jdbi.create(ds)
			.registerRowMapper(ProtoBeanMapper.factory(Contact.class, Contact.Builder.class))
			.registerRowMapper(ProtoBeanMapper.factory(Address.class, Address.Builder.class))
			.installPlugin(new SqlObjectPlugin());
		/* The port on which the RPC server should run */
		int rpcPort = 50051;

		//Todo: Add interceptor to services
		server = ServerBuilder.forPort(rpcPort)
				.addService(ProtoReflectionService.newInstance())
				.addService(intercept(new RestaurantServiceImpl(jdbi), new AuthInterceptor(jdbi, prop)))
				.addService(new ReservationServiceImpl())
				.addService(new UserServiceImpl(jdbi))
				.addService(new AuthServiceImpl(jdbi, prop))
				.build()
				.start();
		logger.info("Server started, listening on " + rpcPort);
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				// Use stderr here since the logger may have been reset by its JVM shutdown hook.
				System.err.println("*** shutting down gRPC server since JVM is shutting down");
				try {
					ApplicationServer.this.stop();
				} catch (InterruptedException e) {
					e.printStackTrace(System.err);
				}
				System.err.println("*** server shut down");
			}
		});
	}

	private void stop() throws InterruptedException {
		if (server != null) {
			server.shutdown().awaitTermination(30, TimeUnit.SECONDS);
		}
	}

	/**
	 * Await termination on the main thread since the grpc library uses daemon threads.
	 */
	private void blockUntilShutdown() throws InterruptedException {
		if (server != null) {
			server.awaitTermination();
		}
	}

	/**
	 * Main launches the server from the command line.
	 */
	public static void main(String[] args) throws IOException, InterruptedException {
		final ApplicationServer server = new ApplicationServer();
		server.start();
		server.blockUntilShutdown();
	}

	static class ReservationServiceImpl extends ReservationServiceGrpc.ReservationServiceImplBase {}
}

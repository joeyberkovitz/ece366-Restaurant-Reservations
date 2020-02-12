package edu.cooper.ece366.restaurantReservation.grpc;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * Server that manages startup/shutdown of a {@code Greeter} server.
 */
public class RestaurantServer {
  private static final Logger logger = Logger.getLogger(RestaurantServer.class.getName());

  private Server server;

  private void start() throws IOException {
    /* The port on which the server should run */
    int port = 50051;

    //Todo: Add interceptor to services
    server = ServerBuilder.forPort(port)
        .addService(new RestaurantServiceImpl())
        .addService(new ReservationServiceImpl())
        .addService(new UserServiceImpl())
        .build()
        .start();
    logger.info("Server started, listening on " + port);
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        // Use stderr here since the logger may have been reset by its JVM shutdown hook.
        System.err.println("*** shutting down gRPC server since JVM is shutting down");
        try {
          RestaurantServer.this.stop();
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
    final RestaurantServer server = new RestaurantServer();
    server.start();
    server.blockUntilShutdown();
  }

  static class RestaurantServiceImpl extends RestaurantServiceGrpc.RestaurantServiceImplBase {

    @Override
    public void createRestaurant(RestaurantServiceOuterClass.Restaurant req, StreamObserver<RestaurantServiceOuterClass.Restaurant> responseObserver) {
      RestaurantServiceOuterClass.Restaurant reply = RestaurantServiceOuterClass.Restaurant.newBuilder().setId(1).build();
      responseObserver.onNext(reply);
      responseObserver.onCompleted();
    }
  }

  static class ReservationServiceImpl extends ReservationServiceGrpc.ReservationServiceImplBase {}
  static class UserServiceImpl extends UserServiceGrpc.UserServiceImplBase {}
}

package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.CreateUserRequest;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

public interface UserDao {
    @SqlUpdate("INSERT INTO user(username, password, fname, lname, contact_id, role_id, rewards_points) VALUES (:username, :password, :fname, :lname, :contact_id, :role_id, :rewards_points)")
    @GetGeneratedKeys("id")
    int insertUser(String username, String password, String fname, String lname, int contact_id, int role_id, int rewards_points);

    default int insertUserDriver(CreateUserRequest userRequest, int roleId) {
        return insertUser(userRequest.getUser().getUsername(), userRequest.getPassword(), userRequest.getUser().getFname(), userRequest.getUser().getLname(), userRequest.getUser().getContact().getId(), roleId, userRequest.getUser().getPoints());
    }
}

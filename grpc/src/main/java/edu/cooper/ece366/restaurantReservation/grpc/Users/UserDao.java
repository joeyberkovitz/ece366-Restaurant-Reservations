package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Auth.DBHashResponse;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import org.jdbi.v3.sqlobject.config.RegisterBeanMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Date;
import java.util.Optional;

public interface UserDao {
    @SqlUpdate("INSERT INTO user(username, password, fname, lname, contact_id, role_id) VALUES (:username, :password, :fname, :lname, :contact_id, :role_id)")
    @GetGeneratedKeys("id")
    int insertUser(String password, int contact_id, @BindBean User user, int role_id);

    /**
     * @param name Username
     * @return bool indicating if username exists
     */
    @SqlQuery("SELECT (COUNT(*) > 0) as userExists FROM user WHERE username = :name")
    boolean userExists(String name);

    @SqlQuery("SELECT id, password FROM user WHERE username = :name")
    @RegisterBeanMapper(DBHashResponse.class)
    Optional<DBHashResponse> getUserHash(String name);

    @SqlQuery("SELECT u.id, u.username, u.fname, u.lname, u.rewards_points as points," +
            " c.phone, c.email, r.name as role" +
            " FROM user u" +
            " INNER JOIN contact c on c.id = u.contact_id " +
            " INNER JOIN role r on r.id = u.role_id " +
            " WHERE u.id = :id")
    @RegisterRowMapper(UserMapper.class)
    User getUser(int id);

    @SqlUpdate("UPDATE user SET fname=coalesce(:fname,fname)," +
            "lname=coalesce(:lname,lname)" +
            "contact_id=coalesce(:contact_id,contact_id" +
            "WHERE id = :id")
    void setUser(@BindBean User user, int contact_id);
    // TODO not set if values same

    @SqlUpdate("INSERT INTO user_login(user_id, refresh_token, user_agent, expiration_date) " +
            "VALUES(:id, :token, :userAgent, :expirationDate)")
    void insertUserToken(int id, String token, String userAgent, Date expirationDate);


    @SqlQuery("SELECT user_id FROM user_login WHERE refresh_token = :token AND revoked = 0")
    Optional<Integer> checkRefreshToken(String token);
}

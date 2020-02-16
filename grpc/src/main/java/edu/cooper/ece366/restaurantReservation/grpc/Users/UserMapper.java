package edu.cooper.ece366.restaurantReservation.grpc.Users;

import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import edu.cooper.ece366.restaurantReservation.grpc.User;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class UserMapper implements RowMapper<User> {

	@Override
	public User map(ResultSet rs, StatementContext ctx) throws SQLException {
		return User.newBuilder()
				.setId(rs.getInt("id"))
				.setUsername(rs.getString("username"))
				.setFname(rs.getString("fname"))
				.setLname(rs.getString("lname"))
				.setPoints(rs.getInt("points"))
				.setContact(Contact.newBuilder()
						.setPhone(rs.getString("phone"))
						.setEmail(rs.getString("email"))
						.build())
				.setRole(User.UserRole.valueOf(rs.getString("role").toUpperCase()))
				.build();
	}
}

package edu.cooper.ece366.restaurantReservation.grpc.Reservations;

import edu.cooper.ece366.restaurantReservation.grpc.*;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

class ReservationMapper implements RowMapper<Reservation> {
	@Override
	public Reservation map(ResultSet rs, StatementContext ctx)
		throws SQLException {
		return Reservation.newBuilder()
			.setId(rs.getInt("id"))
			.setStartTime(rs.getDate("start_time").getTime()/1000)
			// TODO why divide by 1000?
			.setNumPeople(rs.getInt("num_people"))
			.setPoints(rs.getInt("num_points"))
			.setStatus(Reservation.ReservationStatus.valueOf(rs.getString("statusName").toUpperCase()))
			.setRestaurant(Restaurant.newBuilder()
				.setId(rs.getInt("restaurant_id"))
				//Todo: maybe pull full restaurant info
				.build())
			.build();
	}
}

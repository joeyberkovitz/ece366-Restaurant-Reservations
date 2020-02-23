package edu.cooper.ece366.restaurantReservation.grpc;

import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface StatusDao {
    @SqlQuery("SELECT id FROM status WHERE name LIKE :type")
    int getStatusIdByName(String type);
}

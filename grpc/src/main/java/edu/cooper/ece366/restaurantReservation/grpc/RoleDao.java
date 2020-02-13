package edu.cooper.ece366.restaurantReservation.grpc;

import org.jdbi.v3.sqlobject.statement.SqlQuery;

public interface RoleDao {
    @SqlQuery("SELECT id FROM role WHERE type = :type")
    int getRoleIdByName(String type);
}

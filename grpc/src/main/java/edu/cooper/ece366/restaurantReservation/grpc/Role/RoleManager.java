package edu.cooper.ece366.restaurantReservation.grpc.Role;

import org.jdbi.v3.core.Jdbi;

public class RoleManager {
    private Jdbi db;
    public RoleManager(Jdbi db) {
        this.db = db;
    }

    public int getRoleById(String type) {
        return db.withExtension(RoleDao.class, dao -> dao.getRoleIdByName(type));
    }
}

package edu.cooper.ece366.restaurantReservation.grpc.Contacts;

import edu.cooper.ece366.restaurantReservation.grpc.Contact;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Optional;

public interface ContactDao {
    @SqlUpdate("INSERT INTO contact(phone,email) VALUES (:phone,:email)")
    @GetGeneratedKeys("id")
    int insertContact(@BindBean Contact contact);

    @SqlQuery("SELECT id FROM contact WHERE id = :id")
    Optional<Integer> checkContact(int id);

    @SqlQuery("SELECT * FROM contact WHERE id = :id")
    Contact getContact(int id);

    @SqlUpdate("UPDATE contact SET phone = :phone, email = :email WHERE id = :id")
    void setContact(@BindBean Contact contact);

    @SqlUpdate("DELETE FROM contact WHERE id = :id")
    void deleteContact(int id);
}

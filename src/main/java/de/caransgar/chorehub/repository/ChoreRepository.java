package de.caransgar.chorehub.repository;

import de.caransgar.chorehub.entity.Chore;
import de.caransgar.chorehub.entity.RecurrenceType;
import de.caransgar.chorehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChoreRepository extends JpaRepository<Chore, Long> {

    List<Chore> findByAssignedUser(User user);

    List<Chore> findByRecurrenceType(RecurrenceType recurrenceType);

}
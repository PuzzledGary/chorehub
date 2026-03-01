package de.caransgar.chorehub.repository;

import de.caransgar.chorehub.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByNameAndDeletedFalse(String name);

    Optional<User> findByShortnameAndDeletedFalse(String shortname);

    List<User> findByDeletedTrue();

    Optional<User> findByIdAndDeletedFalse(Long id);

    List<User> findByDeletedFalse();

}

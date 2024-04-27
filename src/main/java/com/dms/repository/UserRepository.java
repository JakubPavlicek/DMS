package com.dms.repository;

import com.dms.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * The {@code UserRepository} interface provides access to user-related data stored in the database.
 *
 * @author Jakub Pavlíček
 * @version 1.0
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

}

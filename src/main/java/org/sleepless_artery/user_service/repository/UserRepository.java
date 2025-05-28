package org.sleepless_artery.user_service.repository;

import org.sleepless_artery.user_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmailAddress(String emailAddress);

    Optional<User> findByEmailAddress(String emailAddress);
}

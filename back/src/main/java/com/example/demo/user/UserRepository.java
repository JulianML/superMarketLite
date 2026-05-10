package com.example.demo.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query(value = "SELECT id FROM businesses WHERE owner_user_id = :userId LIMIT 1", nativeQuery = true)
    Optional<Long> findOwnedBusinessId(@Param("userId") Long userId);
}

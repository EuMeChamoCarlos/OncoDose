package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.user;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u JOIN FETCH u.auth WHERE u.auth.username = :username")
    Optional<User> findByAuthUsername(@Param("username") String username);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.auth WHERE u.auth.username = :username")
    boolean existsByAuthUsername(@Param("username") String username);

    @Query("SELECT u FROM User u JOIN FETCH u.auth")
    List<User> findAllEager();
}
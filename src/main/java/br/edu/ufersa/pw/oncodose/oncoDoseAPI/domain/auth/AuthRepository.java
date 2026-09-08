package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AuthRepository extends JpaRepository<Auth, UUID> {

    Optional<Auth> findByUsername(String username);

    boolean existsByUsername(String username);
}
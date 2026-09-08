package br.edu.ufersa.pw.oncodose.oncoDoseAPI.domain.privilege;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PrivilegeRepository extends JpaRepository<Privilege, UUID> {
    Page<Privilege> findAll(Pageable pageable);


    Optional<Privilege> findByName(String name);

    boolean existsByName(String name);
}
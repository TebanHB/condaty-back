package com.condaty.auth_service.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.condaty.auth_service.entity.Person;

@Repository
public interface PersonRepository extends JpaRepository<Person, String> {
    
    Optional<Person> findByUuid(String uuid);
    
    Optional<Person> findByEmail(String email);
    
    boolean existsByUuid(String uuid);
    
    boolean existsByEmail(String email);
}

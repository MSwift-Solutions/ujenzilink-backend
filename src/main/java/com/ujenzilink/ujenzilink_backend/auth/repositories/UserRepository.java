package com.ujenzilink.ujenzilink_backend.auth.repositories;


import com.ujenzilink.ujenzilink_backend.auth.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findFirstByEmail(String email);
}

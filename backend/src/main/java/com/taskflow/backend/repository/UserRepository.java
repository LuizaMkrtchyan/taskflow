package com.taskflow.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.taskflow.backend.entity.User;

import java.util.Optional;

//JPARepository<entity,type of entity's id>
//automatically provides save,findById,findAll,delete
//added - find by Email
public interface UserRepository extends JpaRepository<User,Long> {
    Optional<User> findByEmail(String email);
}

package com.demo.springbatch.repository;

import com.demo.springbatch.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    boolean existsByEmail(String email); // To check duplicates
}

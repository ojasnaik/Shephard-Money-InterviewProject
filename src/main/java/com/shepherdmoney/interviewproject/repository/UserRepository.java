package com.shepherdmoney.interviewproject.repository;

import com.shepherdmoney.interviewproject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for User entities.
 * This interface extends JpaRepository and provides methods to interact with the database.
 * It is annotated with @Repository to indicate that it's a Bean and its role is to interact with the database.
 * The UserRepository interface is used to store User entities.
 */
@Repository("UserRepo")
public interface UserRepository extends JpaRepository<User, Integer> {
}
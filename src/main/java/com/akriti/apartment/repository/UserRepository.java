package com.akriti.apartment.repository;

import com.akriti.apartment.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByPhone(String phone);
    List<User> findByFlatNo(String flatNo);
    boolean existsByPhone(String phone);
}

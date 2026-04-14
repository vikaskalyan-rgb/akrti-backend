package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Idea;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IdeaRepository extends JpaRepository<Idea, Long> {
    List<Idea> findAllByOrderByCreatedAtDesc();
}
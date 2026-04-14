package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Idea;
import com.akriti.apartment.entity.IdeaUpvote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IdeaUpvoteRepository extends JpaRepository<IdeaUpvote, Long> {
    int countByIdea(Idea idea);
    boolean existsByIdeaAndFlatNo(Idea idea, String flatNo);
    void deleteByIdeaAndFlatNo(Idea idea, String flatNo);
    void deleteByIdeaId(Long ideaId);
}
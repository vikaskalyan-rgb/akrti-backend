package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Idea;
import com.akriti.apartment.entity.IdeaComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface IdeaCommentRepository extends JpaRepository<IdeaComment, Long> {
    List<IdeaComment> findByIdeaOrderByCreatedAtAsc(Idea idea);
    int countByIdea(Idea idea);
    void deleteByIdeaId(Long ideaId);
}
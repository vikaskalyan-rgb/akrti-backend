package com.akriti.apartment.repository;

import com.akriti.apartment.entity.CommunityPost;
import com.akriti.apartment.entity.CommunityPostComment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommunityPostCommentRepository extends JpaRepository<CommunityPostComment, Long> {
    List<CommunityPostComment> findByPostOrderByCreatedAtAsc(CommunityPost post);
    int countByPost(CommunityPost post);
    void deleteByPostId(Long postId);
}
package com.akriti.apartment.repository;

import com.akriti.apartment.entity.CommunityPost;
import com.akriti.apartment.entity.CommunityPostLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommunityPostLikeRepository extends JpaRepository<CommunityPostLike, Long> {
    int countByPost(CommunityPost post);
    boolean existsByPostAndFlatNo(CommunityPost post, String flatNo);
    void deleteByPostAndFlatNo(CommunityPost post, String flatNo);
    void deleteByPostId(Long postId);
}
package com.akriti.apartment.repository;

import com.akriti.apartment.entity.Announcement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {
    List<Announcement> findAllByOrderByIsPinnedDescPostedAtDesc();
    List<Announcement> findByAudienceInOrderByIsPinnedDescPostedAtDesc(List<Announcement.Audience> audiences);
}

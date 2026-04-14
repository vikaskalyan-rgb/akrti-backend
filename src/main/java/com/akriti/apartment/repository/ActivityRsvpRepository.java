package com.akriti.apartment.repository;

import com.akriti.apartment.entity.ActivityRsvp;
import com.akriti.apartment.entity.WeeklyActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivityRsvpRepository extends JpaRepository<ActivityRsvp, Long> {
    List<ActivityRsvp> findByActivity(WeeklyActivity activity);
    int countByActivity(WeeklyActivity activity);
    boolean existsByActivityAndFlatNo(WeeklyActivity activity, String flatNo);
    void deleteByActivityAndFlatNo(WeeklyActivity activity, String flatNo);
    void deleteByActivityId(Long activityId);
}
package com.akriti.apartment.repository;

import com.akriti.apartment.entity.StepLog;
import com.akriti.apartment.entity.StepWalker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface StepLogRepository extends JpaRepository<StepLog, Long> {

    Optional<StepLog> findByWalkerAndLogDate(StepWalker walker, LocalDate logDate);

    List<StepLog> findByWalkerOrderByLogDateDesc(StepWalker walker);

    @Query("""
        SELECT sl.walker.id, sl.walker.flatNo, sl.walker.walkerName, COALESCE(SUM(sl.steps), 0)
        FROM StepLog sl
        WHERE sl.logDate BETWEEN :from AND :to
        GROUP BY sl.walker.id, sl.walker.flatNo, sl.walker.walkerName
        ORDER BY SUM(sl.steps) DESC
    """)
    List<Object[]> leaderboard(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );

    @Query("""
        SELECT sl.walker.flatNo, sl.walker.walkerName, COALESCE(SUM(sl.steps), 0)
        FROM StepLog sl
        WHERE sl.logDate BETWEEN :from AND :to
        GROUP BY sl.walker.flatNo, sl.walker.walkerName
        ORDER BY SUM(sl.steps) DESC
    """)
    List<Object[]> monthlyLeaderboard(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to
    );
}
package com.estore.library.repository.log;

import com.estore.library.model.log.LoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoginLogRepository extends JpaRepository<LoginLog, Long> {

    List<LoginLog> findByUserId(UUID userId);

    @Query("SELECT EXTRACT(HOUR FROM l.loggedAt) as hour, COUNT(l) as cnt " +
            "FROM LoginLog l WHERE l.loggedAt >= :from GROUP BY hour ORDER BY hour")
    List<Object[]> countByHourSince(LocalDateTime from);
}


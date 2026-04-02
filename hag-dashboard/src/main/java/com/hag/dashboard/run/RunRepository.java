package com.hag.dashboard.run;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RunRepository extends JpaRepository<RunRecord, String> {
    List<RunRecord> findAllByOrderByStartedAtDesc();
    List<RunRecord> findByStatusOrderByStartedAtDesc(String status);
}

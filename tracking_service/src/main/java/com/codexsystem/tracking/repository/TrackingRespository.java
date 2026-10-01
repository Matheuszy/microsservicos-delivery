package com.codexsystem.tracking.repository;

import com.codexsystem.tracking.model.Tracking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrackingRespository extends JpaRepository<Tracking, Long> {
}

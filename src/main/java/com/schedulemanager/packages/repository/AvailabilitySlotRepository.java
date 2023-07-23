package com.schedulemanager.packages.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

	List<AvailabilitySlot> findByCandidate(Candidate candidate);
}
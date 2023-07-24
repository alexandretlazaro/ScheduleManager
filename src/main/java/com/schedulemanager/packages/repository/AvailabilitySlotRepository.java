package com.schedulemanager.packages.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

	List<AvailabilitySlot> findByCandidate(Candidate candidate);
	
	@Query("SELECT COUNT(a) FROM AvailabilitySlot a " +
	           "WHERE a.interviewer.id = :interviewerId " +
	           "AND a.dayOfWeek = :dayOfWeek " +
	           "AND ((a.startTime <= :startTime AND a.endTime >= :startTime) " +
	           "OR (a.startTime <= :endTime AND a.endTime >= :endTime))")
	    Integer countConflictingTimeSlotsForInterviewer(@Param("interviewerId") Long interviewerId,
	                                   @Param("dayOfWeek") DayOfWeek dayOfWeek,
	                                   @Param("startTime") LocalTime startTime,
	                                   @Param("endTime") LocalTime endTime);

	@Query("SELECT COUNT(a) FROM AvailabilitySlot a " +
	           "WHERE a.candidate.id = :candidateId " +
	           "AND a.dayOfWeek = :dayOfWeek " +
	           "AND ((a.startTime <= :startTime AND a.endTime >= :startTime) " +
	           "OR (a.startTime <= :endTime AND a.endTime >= :endTime))")
	Integer countConflictingTimeSlotsForCandidate(Long candidateId, DayOfWeek dayOfWeek, LocalTime startTime,
			LocalTime endTime);
}
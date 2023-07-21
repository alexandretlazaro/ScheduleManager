package com.schedulemanager.packages.repository;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.domain.Interviewer;

public interface AvailabilitySlotRepository extends JpaRepository<AvailabilitySlot, Long> {

	List<AvailabilitySlot> findByCandidate(Candidate candidate);

	@Query("select a from AvailabilitySlot a where a.interviewer = :interviewer and a.startTime in :startTimes and a.endTime in :endTimes and a.dayOfWeek in :dayOfWeeks ")
	List<AvailabilitySlot> findByInterviewerAndTime(
			@Param("interviewer") Interviewer interviewer,
			@Param("startTimes") List<LocalTime> startTimes,
			@Param("endTimes") List<LocalTime> endTimes,
			@Param("dayOfWeeks") List<DayOfWeek> dayOfWeeks);
}

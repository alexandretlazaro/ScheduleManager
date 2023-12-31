package com.schedulemanager.packages.service;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.repository.AvailabilitySlotRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AvailabilitySlotService {

	@Autowired
	private AvailabilitySlotRepository repository;

	public List<AvailabilitySlot> getAvailabilitySlots() {

		return repository.findAll();
	}

	public Optional<AvailabilitySlot> getAvailabilitySlotById(Long id) {

		Optional<AvailabilitySlot> optional = Optional.ofNullable(repository.findById(id).orElseThrow(() -> 
		new EntityNotFoundException("Availability Slot with ID " + id + "  not found")));
		
		return optional;

	}

	public AvailabilitySlot save(AvailabilitySlot availabilitySlot) {

		return repository.save(availabilitySlot);
	}

	public List<AvailabilitySlot> findByCandidate(Candidate candidate) {
		return repository.findByCandidate(candidate);
	}
	
	public boolean isTimeAlreadyExistsForInterviewer(Long interviewerId, DayOfWeek dayOfWeek, LocalTime startTime,
			LocalTime endTime) {

		Integer count = repository.countConflictingTimeSlotsForInterviewer(interviewerId, dayOfWeek, startTime, endTime);
		
		return count > 0;
	}

	public boolean isTimeAlreadyExistsForCandidate(Long candidateId, DayOfWeek dayOfWeek, LocalTime startTime,
			LocalTime endTime) {

		Integer count = repository.countConflictingTimeSlotsForCandidate(candidateId, dayOfWeek, startTime, endTime);
		
		return count > 0;
	
	}

}

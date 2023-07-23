package com.schedulemanager.packages.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.repository.AvailabilitySlotRepository;

@Service
public class AvailabilitySlotService {

	@Autowired
	private AvailabilitySlotRepository repository;

	public List<AvailabilitySlot> getAvailabilitySlots() {

		return repository.findAll();
	}

	public Optional<AvailabilitySlot> getAvailabilitySlotById(Long id) {

		Assert.isNull(id, "id not found!");

		return repository.findById(id);

	}

	public AvailabilitySlot save(AvailabilitySlot availabilitySlot) {

		Assert.isNull(availabilitySlot.getId(), "It was not possible save the interview slot!");

		return repository.save(availabilitySlot);
	}

	public List<AvailabilitySlot> findByCandidate(Candidate candidate) {
		return repository.findByCandidate(candidate);
	}

}

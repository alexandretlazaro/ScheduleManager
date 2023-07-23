package com.schedulemanager.packages.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.dto.CandidateDTO;
import com.schedulemanager.packages.repository.AvailabilitySlotRepository;
import com.schedulemanager.packages.repository.CandidateRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CandidateService {

	@Autowired
	private CandidateRepository repository;

	@Autowired
	private AvailabilitySlotRepository availabilitySlotRepository;

	public List<Candidate> getCandidates() {

		return repository.findAll();
	}

	public Optional<Candidate> getCandidateById(Long id) {

		return repository.findById(id);

	}

	public Candidate save(Candidate candidate) {

		return repository.save(candidate);
	}

	public CandidateDTO update(Long id, Candidate candidate) {

		Optional<Candidate> candidateOptional = Optional.ofNullable(repository.findById(id).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + id + " not found")));

		Candidate candidateDb = candidateOptional.get();

		candidateDb.setName(candidate.getName());

		repository.save(candidate);

		return CandidateDTO.create(candidateDb);
	}

	public boolean isTimeSlotAlreadyExists(Long id, AvailabilitySlot slot) {

		Optional<Candidate> candidateOptional = Optional.ofNullable(repository.findById(id).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + id + " not found")));


		Candidate candidateDb = candidateOptional.get();

		return candidateDb.getInterviewSlotList().stream()
				.anyMatch(i -> i.getDayOfWeek().equals(slot.getDayOfWeek()) &&
						i.getStartTime() == slot.getStartTime() &&
						i.getEndTime() == slot.getEndTime());


	}

	public void delete(Long id) {

		repository.deleteById(id);
	}

	public void deleteSlot(Long candidateId, Long availabilitySlotId) {

		Optional<Candidate> candidateOptional = Optional.ofNullable(repository.findById(candidateId).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + candidateId + " not found")));

		Candidate candidateDb = candidateOptional.get();

		AvailabilitySlot candidateSlot = candidateDb.getInterviewSlotList().stream()
				.filter(a -> a.getId() == availabilitySlotId)
				.findFirst()
				.orElseThrow(() ->
				new EntityNotFoundException("Availability's id not found"));

		candidateDb.getInterviewSlotList().remove(candidateSlot);

		availabilitySlotRepository.deleteById(availabilitySlotId);
		repository.save(candidateDb);

	}

}

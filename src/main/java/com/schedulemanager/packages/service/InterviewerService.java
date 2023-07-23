package com.schedulemanager.packages.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.dto.InterviewerDTO;
import com.schedulemanager.packages.repository.AvailabilitySlotRepository;
import com.schedulemanager.packages.repository.InterviewerRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class InterviewerService {

	@Autowired
	private InterviewerRepository repository;

	@Autowired
	private AvailabilitySlotRepository availabilitySlotRepository;

	public List<Interviewer> getInterviewers() {

		return repository.findAll();
	}

	public Optional<Interviewer> getInterviewerById(Long id) {

		return repository.findById(id);

	}

	public Interviewer save(Interviewer interviewer) {

		return repository.save(interviewer);
	}

	public List<Interviewer> getInterviewersByIds(List<Long> interviewerIds) {
		return repository.findAllById(interviewerIds);
	}

	public InterviewerDTO update(Long interviewerId, Interviewer interviewer) {

		Optional<Interviewer> interviewerOptional = Optional.ofNullable(repository.findById(interviewerId).orElseThrow(() -> 
		new EntityNotFoundException("Interviewer with ID " + interviewerId + "  not found")));

		Interviewer interviewerDb = interviewerOptional.get();

		interviewerDb.setName(interviewer.getName());

		repository.save(interviewer);

		return InterviewerDTO.create(interviewerDb);

	}

	public void delete(Long id) {

		if(this.getInterviewerById(id).isPresent()) {
			repository.deleteById(id);
		}
	}

	public void deleteSlot(Long interviewerId, Long availabilitySlotId) {

		Optional<Interviewer> interviewerOptional = Optional.ofNullable(repository.findById(interviewerId).orElseThrow(() -> 
		new EntityNotFoundException("Interviewer with ID " + interviewerId + "  not found")));

		Interviewer interviewerDb = interviewerOptional.get();

		AvailabilitySlot interviewerSlot = interviewerDb.getAvailabilitySlotList().stream()
				.filter(a -> a.getId() == availabilitySlotId)
				.findFirst()
				.orElseThrow(() ->
				new EntityNotFoundException("Availability's id not found"));

		interviewerDb.getAvailabilitySlotList().remove(interviewerSlot);

		availabilitySlotRepository.deleteById(availabilitySlotId);
		repository.save(interviewerDb);

	}


}

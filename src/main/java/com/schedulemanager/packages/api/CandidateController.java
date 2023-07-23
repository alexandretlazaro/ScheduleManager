package com.schedulemanager.packages.api;

import java.net.URI;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO;
import com.schedulemanager.packages.dto.CandidateDTO;
import com.schedulemanager.packages.service.AvailabilitySlotService;
import com.schedulemanager.packages.service.CandidateService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/schedule/candidate")
public class CandidateController {

	@Autowired
	CandidateService candidateService;

	@Autowired
	AvailabilitySlotService slotService;

	@GetMapping("/")
	public List<CandidateDTO> getAllCandidates() {

		List<Candidate> candidates = candidateService.getCandidates();

		List<CandidateDTO> candidateDTOsList = new ArrayList<>();

		for (Candidate candidate : candidates) {

			CandidateDTO candidateDTO = new CandidateDTO();

			candidateDTO.setId(candidate.getId());
			candidateDTO.setName(candidate.getName());

			List<AvailabilitySlot> availabilitySlotsList = candidate.getInterviewSlotList();

			List<AvailabilitySlotDTO> availabilitySlotDTOList = new ArrayList<>();

			for (AvailabilitySlot availabilitySlot : availabilitySlotsList) {

				AvailabilitySlotDTO dto = new AvailabilitySlotDTO();

				dto.setDayOfWeek(availabilitySlot.getDayOfWeek());
				dto.setStartTime(availabilitySlot.getStartTime());
				dto.setEndTime(availabilitySlot.getEndTime());

				availabilitySlotDTOList.add(dto);
			}

			candidateDTO.setAvailabilitySlotDTOs(availabilitySlotDTOList);

			candidateDTOsList.add(candidateDTO);
		}

		return candidateDTOsList;
	}

	@GetMapping("/{id}")
	public ResponseEntity<Candidate> getCandidateById(@PathVariable Long id) {

		Optional<Candidate> candidateOptional = Optional.ofNullable(candidateService.getCandidateById(id).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + id + " not found")));

		return ResponseEntity.ok(candidateOptional.get());
	}

	@PostMapping
	public ResponseEntity<Candidate> saveCandidate(@RequestBody Candidate candidate) {

		Candidate i = candidateService.save(candidate);

		URI location = getURI(i.getId());

		return ResponseEntity.created(location).build();
	}

	@PostMapping("{candidateId}/candidate-slot")
	public ResponseEntity<Candidate> saveCandidateAvailabilitySlot(@PathVariable Long candidateId, @RequestBody AvailabilitySlot availabilitySlot) {

		List<AvailabilitySlot> interviewSlots = new ArrayList<>();

		if(this.localTimeValidate(availabilitySlot)) {

			if (candidateService.isTimeSlotAlreadyExists(candidateId, availabilitySlot)) {
				return ResponseEntity.badRequest().body(null);
			}

			Optional<Candidate> candidateOptional = Optional.ofNullable(candidateService.getCandidateById(candidateId).orElseThrow(() -> 
			new EntityNotFoundException("Candidate with ID " + candidateId + " not found")));

			LocalTime startTime = availabilitySlot.getStartTime();
			LocalTime endTime = availabilitySlot.getEndTime();

			LocalTime slot1Hour = startTime;

			while(slot1Hour.isBefore(endTime)) {

				AvailabilitySlot slot = new AvailabilitySlot();

				LocalTime currentStart = slot1Hour;
				LocalTime currentEndTime = currentStart.plusHours(1);

				slot.setCandidate(candidateOptional.get());
				slot.setDayOfWeek(availabilitySlot.getDayOfWeek());
				slot.setStartTime(currentStart);
				slot.setEndTime(currentEndTime);

				interviewSlots.add(slot);

				slot1Hour = currentEndTime;

			}

			List<AvailabilitySlot> savedSlots = interviewSlots.stream().map(slotService::save).collect(Collectors.toList());

			Candidate savedCandidate = candidateOptional.get();
			savedCandidate.setInterviewSlotList(savedSlots);

			return ResponseEntity.ok(savedCandidate);
		}

		return ResponseEntity.badRequest().build();
	}

	@PatchMapping("{id}")
	public ResponseEntity<CandidateDTO> updateCandidate(@PathVariable Long id, @RequestBody Candidate candidate) {

		candidate.setId(id);

		CandidateDTO dto = candidateService.update(id, candidate);

		return dto != null ?
				ResponseEntity.ok(dto) :
					ResponseEntity.notFound().build();
	}

	@DeleteMapping("{id}")
	public ResponseEntity<CandidateDTO> deleteCandidate(@PathVariable Long id) {

		Optional<Candidate> candidateOptional = Optional.ofNullable(candidateService.getCandidateById(id).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + id + " not found")));
		
		candidateService.delete(candidateOptional.get().getId());

		return ResponseEntity.ok().build();
	}

	@DeleteMapping("{candidateId}/candidate-slot/{availabilitySlotId}")
	public ResponseEntity<CandidateDTO> deleteCandidateSlot(@PathVariable Long candidateId, @PathVariable Long availabilitySlotId) {

		Optional<Candidate> candidateOptional = Optional.ofNullable(candidateService.getCandidateById(candidateId).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + candidateId + " not found")));
		
		candidateService.deleteSlot(candidateOptional.get().getId(), availabilitySlotId);

		return ResponseEntity.ok().build();
	}

	private boolean localTimeValidate(AvailabilitySlot availabilitySlot) {

		boolean isValid = true;

		LocalTime startTime = availabilitySlot.getStartTime();
		LocalTime endTime = availabilitySlot.getEndTime();

		if(endTime.isBefore(startTime)) {
			isValid = false;
		}

		if(!(startTime.getMinute() == 0 && startTime.getSecond() == 0)) {

			isValid = false;
		}

		if(!(endTime.getMinute() == 0 && endTime.getSecond() == 0)) {

			isValid = false;
		}

		return isValid;
	}

	private URI getURI(Long id) {

		return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<String> handleResourceNotFoundException(NoSuchElementException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Element not found");
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Informed ID does not exist");
	}
}

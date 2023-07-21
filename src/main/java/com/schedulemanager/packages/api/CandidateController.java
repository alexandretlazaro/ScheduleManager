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
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
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

		Optional<Candidate> c = candidateService.getCandidateById(id);

		if(!c.isPresent()) {
			return ResponseEntity.notFound().build();
		}

		return ResponseEntity.ok(c.get());
	}

	@PostMapping
	public ResponseEntity<Candidate> saveCandidate(@RequestBody Candidate candidate) {

		Candidate i = candidateService.save(candidate);

		URI location = getURI(i.getId());

		return ResponseEntity.created(location).build();
	}

	@PostMapping("/candidate-slot/{candidateId}")
	public ResponseEntity<Candidate> saveInterviewerAvailabilitySlot(@PathVariable Long candidateId, @RequestBody AvailabilitySlot availabilitySlot) {

		List<AvailabilitySlot> interviewSlots = new ArrayList<>();
		
		if(this.localTimeValidate(availabilitySlot)) {

			Optional<Candidate> candidate = candidateService.getCandidateById(candidateId);

			LocalTime startTime = availabilitySlot.getStartTime();
			LocalTime endTime = availabilitySlot.getEndTime();
			
			LocalTime slot1Hour = startTime;
			
			while(slot1Hour.isBefore(endTime)) {
				
				AvailabilitySlot slot = new AvailabilitySlot();
				
				LocalTime currentStart = slot1Hour;
				LocalTime currentEndTime = currentStart.plusHours(1);
				
				slot.setCandidate(candidate.get());
				slot.setDayOfWeek(availabilitySlot.getDayOfWeek());
				slot.setStartTime(currentStart);
				slot.setEndTime(currentEndTime);
				
				interviewSlots.add(slot);
				
				slot1Hour = currentEndTime;
				
			}
			
			List<AvailabilitySlot> savedSlots = interviewSlots.stream().map(slotService::save).collect(Collectors.toList());

			Candidate savedCandidate = candidate.get();
			savedCandidate.setInterviewSlotList(savedSlots);

			return ResponseEntity.ok(savedCandidate);
		}

		return ResponseEntity.badRequest().build();
	}

	private boolean localTimeValidate(AvailabilitySlot availabilitySlot) {

		boolean isValid = true;

		LocalTime startTime = availabilitySlot.getStartTime();
		LocalTime endTime = availabilitySlot.getEndTime();

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
}

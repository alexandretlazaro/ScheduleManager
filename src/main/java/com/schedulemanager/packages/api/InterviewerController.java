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

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO;
import com.schedulemanager.packages.dto.InterviewerDTO;
import com.schedulemanager.packages.service.AvailabilitySlotService;
import com.schedulemanager.packages.service.InterviewerService;
import com.schedulemanager.packages.utils.Utils;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/schedule/interviewer")
public class InterviewerController {

	@Autowired
	InterviewerService interviewerService;

	@Autowired
	AvailabilitySlotService slotService;

	@GetMapping("/")
	public ResponseEntity<List<InterviewerDTO>> getAllInterviewers() {

		List<Interviewer> interviewers = interviewerService.getInterviewers();

		List<InterviewerDTO> interviewerDTOsList = new ArrayList<>();

		for (Interviewer interviewer : interviewers) {

			InterviewerDTO interviewerDTO = new InterviewerDTO();

			interviewerDTO.setId(interviewer.getId());
			interviewerDTO.setName(interviewer.getName());

			List<AvailabilitySlot> availabilitySlotsList = interviewer.getAvailabilitySlotList();

			List<AvailabilitySlotDTO> availabilitySlotDTOList = new ArrayList<>();

			for (AvailabilitySlot availabilitySlot : availabilitySlotsList) {

				AvailabilitySlotDTO dto = new AvailabilitySlotDTO();

				dto.setDayOfWeek(availabilitySlot.getDayOfWeek());
				dto.setStartTime(availabilitySlot.getStartTime());
				dto.setEndTime(availabilitySlot.getEndTime());

				availabilitySlotDTOList.add(dto);
			}

			interviewerDTO.setAvailabilitySlotDTOs(availabilitySlotDTOList);

			interviewerDTOsList.add(interviewerDTO);
		}

		return ResponseEntity.ok(interviewerDTOsList);
	}

	@GetMapping("/{interviewerId}")
	public ResponseEntity<Interviewer> getInterviewerById(@PathVariable Long interviewerId) {

		Optional<Interviewer> interviewerOptional = interviewerService.getInterviewerById(interviewerId);

		return ResponseEntity.ok(interviewerOptional.get());
	}

	@PostMapping
	public ResponseEntity<Interviewer> saveInterviewer(@RequestBody Interviewer interviewer) {

		Interviewer i = interviewerService.save(interviewer);

		URI location = Utils.getURI(i.getId());

		return ResponseEntity.created(location).build();
	}

	@PostMapping("{interviewerId}/interviewer-slot")
	public ResponseEntity<Interviewer> saveInterviewerAvailabilitySlot(@PathVariable Long interviewerId, @RequestBody AvailabilitySlot availabilitySlot) {

		List<AvailabilitySlot> interviewSlots = new ArrayList<>();

		if(Utils.localTimeValidate(availabilitySlot)) {

			Optional<Interviewer> interviewerOptional = interviewerService.getInterviewerById(interviewerId);
			
			LocalTime startTime = availabilitySlot.getStartTime();
			LocalTime endTime = availabilitySlot.getEndTime();

			boolean timeAlreadyExists = slotService.isTimeAlreadyExistsForInterviewer(interviewerId, 
					availabilitySlot.getDayOfWeek(),
					availabilitySlot.getStartTime(),
					availabilitySlot.getEndTime());
	        
			if (timeAlreadyExists) {
	            throw new IllegalArgumentException("Time already exists for the interviewer on this day.");
	        }
			
			LocalTime slot1Hour = startTime;

			while(slot1Hour.isBefore(endTime)) {

				AvailabilitySlot slot = new AvailabilitySlot();

				LocalTime currentStart = slot1Hour;
				LocalTime currentEndTime = currentStart.plusHours(1);

				slot.setInterviewer(interviewerOptional.get());
				slot.setDayOfWeek(availabilitySlot.getDayOfWeek());
				slot.setStartTime(currentStart);
				slot.setEndTime(currentEndTime);

				interviewSlots.add(slot);

				slot1Hour = currentEndTime;
			}

			List<AvailabilitySlot> savedSlots = interviewSlots.stream().map(slotService::save).collect(Collectors.toList());

			Interviewer savedInterviewer = interviewerOptional.get();
			savedInterviewer.setAvailabilitySlotList(savedSlots);

			return ResponseEntity.ok(savedInterviewer);
		}

		return ResponseEntity.badRequest().build();
	}

	@PatchMapping("/interviewer-slot/{id}")
	public ResponseEntity<InterviewerDTO> updateInterivewer(@PathVariable Long id, @RequestBody Interviewer interviewer) {

		Optional<Interviewer> interviewerOptional = interviewerService.getInterviewerById(id);
		
		interviewer.setId(interviewerOptional.get().getId());

		InterviewerDTO dto = interviewerService.update(id, interviewer);

		return dto != null ?
				ResponseEntity.ok(dto) :
					ResponseEntity.notFound().build();
	}

	@DeleteMapping("{id}")
	public ResponseEntity<InterviewerDTO> deleteInterviewer(@PathVariable Long id) {

		Optional<Interviewer> interviewerOptional = interviewerService.getInterviewerById(id);
		
		interviewerService.delete(interviewerOptional.get().getId());

		return ResponseEntity.ok().build();
	}

	@DeleteMapping("{interviewerId}/interviewer-slot/{availabilitySlotId}")
	public ResponseEntity<InterviewerDTO> deleteInterviewerSlot(@PathVariable Long interviewerId, @PathVariable Long availabilitySlotId) {

		Optional<Interviewer> interviewerOptional = interviewerService.getInterviewerById(interviewerId);
		Optional<AvailabilitySlot> availabilitySlotOptional = slotService.getAvailabilitySlotById(availabilitySlotId);
		
		interviewerService.deleteSlot(interviewerOptional.get().getId(), availabilitySlotOptional.get().getId());

		return ResponseEntity.ok().build();
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<String> handleResourceNotFoundException(NoSuchElementException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
	}

	@ExceptionHandler(EntityNotFoundException.class)
	public ResponseEntity<String> handleEntityNotFoundException(EntityNotFoundException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}

	@ExceptionHandler(IllegalArgumentException.class)
	public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException ex) {
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
	}
}

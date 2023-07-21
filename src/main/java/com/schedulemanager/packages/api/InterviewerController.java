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
import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO;
import com.schedulemanager.packages.dto.InterviewerDTO;
import com.schedulemanager.packages.service.AvailabilitySlotService;
import com.schedulemanager.packages.service.InterviewerService;

@RestController
@RequestMapping("/api/v1/schedule/interviewer")
public class InterviewerController {

	@Autowired
	InterviewerService interviewerService;

	@Autowired
	AvailabilitySlotService slotService;

	@GetMapping("/")
	public List<InterviewerDTO> getAllInterviewers() {

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

		return interviewerDTOsList;
	}

	@GetMapping("/{id}")
	public ResponseEntity<Interviewer> getInterviewerById(@PathVariable Long id) {

		Optional<Interviewer> i = interviewerService.getInterviewerById(id);

		return ResponseEntity.ok(i.get());
	}

	@PostMapping
	public ResponseEntity<Interviewer> saveInterviewer(@RequestBody Interviewer interviewer) {

		Interviewer i = interviewerService.save(interviewer);

		URI location = getURI(i.getId());

		return ResponseEntity.created(location).build();
	}

	@PostMapping("/interviewer-slot/{interviewerId}")
	public ResponseEntity<Interviewer> saveInterviewerAvailabilitySlot(@PathVariable Long interviewerId, @RequestBody AvailabilitySlot availabilitySlot) {

		List<AvailabilitySlot> interviewSlots = new ArrayList<>();
		
		if(this.localTimeValidate(availabilitySlot)) {

			Optional<Interviewer> interviewer = interviewerService.getInterviewerById(interviewerId);

			LocalTime startTime = availabilitySlot.getStartTime();
			LocalTime endTime = availabilitySlot.getEndTime();
			
			LocalTime slot1Hour = startTime;
			
			while(slot1Hour.isBefore(endTime)) {
				
				AvailabilitySlot slot = new AvailabilitySlot();
				
				LocalTime currentStart = slot1Hour;
				LocalTime currentEndTime = currentStart.plusHours(1);
				
				slot.setInterviewer(interviewer.get());
				slot.setDayOfWeek(availabilitySlot.getDayOfWeek());
				slot.setStartTime(currentStart);
				slot.setEndTime(currentEndTime);
				
				interviewSlots.add(slot);
				
				slot1Hour = currentEndTime;
				
			}
			
			List<AvailabilitySlot> savedSlots = interviewSlots.stream().map(slotService::save).collect(Collectors.toList());

			Interviewer savedInterviewer = interviewer.get();
			savedInterviewer.setAvailabilitySlotList(savedSlots);

			return ResponseEntity.ok(savedInterviewer);
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

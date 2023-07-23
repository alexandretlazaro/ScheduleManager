package com.schedulemanager.packages.api;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO.CandidateInfoDTO;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO.InterviewerInfoDTO;
import com.schedulemanager.packages.service.AvailabilitySlotService;
import com.schedulemanager.packages.service.CandidateService;
import com.schedulemanager.packages.service.InterviewerService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/v1/schedule/interview")
public class InterviewController {

	@Autowired
	AvailabilitySlotService availabilitySlotService;

	@Autowired
	InterviewerService interviewerService;

	@Autowired
	CandidateService candidateService;

	@GetMapping("/")
	public AvailabilitySlotDTO getAllAvailabilitySlots (
			@RequestParam Long candidateId,
			@RequestParam List<Long> interviewerIds) {

		AvailabilitySlotDTO availabilitySlotDTO = new AvailabilitySlotDTO();

		List<Interviewer> interviewersList = new ArrayList<>();

		List<AvailabilitySlot> matchedSlotsList = new ArrayList<>();

		Optional<Candidate> candidateOptional = Optional.ofNullable(candidateService.getCandidateById(candidateId).orElseThrow(() -> 
		new EntityNotFoundException("Candidate with ID " + candidateId + " not found")));

		Candidate candidate = candidateOptional.get();

		List<AvailabilitySlot> candidateSlotList = candidate.getInterviewSlotList();

		for (Long interviewerId : interviewerIds) {

			Optional<Interviewer> interviewerOptional = Optional.ofNullable(interviewerService.getInterviewerById(interviewerId).orElseThrow(() -> 
			new EntityNotFoundException("Interviewer with ID " + interviewerId + "  not found")));

			Interviewer interviewer = interviewerOptional.get();

			interviewersList.add(interviewer);
		}
		
		List<CandidateInfoDTO> candidateInfoList = new ArrayList<>();

		for (AvailabilitySlot candidateAvailabilitySlot : candidateSlotList) {
			
			CandidateInfoDTO candidateInfoDTO = new CandidateInfoDTO();
			
			candidateInfoDTO.setCandidateName(candidate.getName());
			candidateInfoDTO.setDayOfWeek(candidateAvailabilitySlot.getDayOfWeek());
			candidateInfoDTO.setStartTime(candidateAvailabilitySlot.getStartTime());
			candidateInfoDTO.setEndTime(candidateAvailabilitySlot.getEndTime());
			
			candidateInfoList.add(candidateInfoDTO);
			
			for (Interviewer interviewer : interviewersList) {

				for (AvailabilitySlot interviewerAvailabilitySlot : interviewer.getAvailabilitySlotList()) {

					if(isTimeIsEquals(candidateAvailabilitySlot.getDayOfWeek(), interviewerAvailabilitySlot.getDayOfWeek(),
							candidateAvailabilitySlot.getStartTime(), interviewerAvailabilitySlot.getStartTime(), 
							candidateAvailabilitySlot.getEndTime(), interviewerAvailabilitySlot.getEndTime())) {

						matchedSlotsList.add(interviewerAvailabilitySlot);
					}
				}
			}
		}

		List<InterviewerInfoDTO> interviewerInfoList = new ArrayList<>();

		for (AvailabilitySlot interviewerAvailabilitySlot : matchedSlotsList) {

			InterviewerInfoDTO interviewerInfo = new InterviewerInfoDTO();

			interviewerInfo.setInterviewerName(interviewerAvailabilitySlot.getInterviewer().getName());
			interviewerInfo.setDayOfWeek(interviewerAvailabilitySlot.getDayOfWeek());
			interviewerInfo.setStartTime(interviewerAvailabilitySlot.getStartTime());
			interviewerInfo.setEndTime(interviewerAvailabilitySlot.getEndTime());

			interviewerInfoList.add(interviewerInfo);
		}

		availabilitySlotDTO.setCandidateInfoList(candidateInfoList);
		availabilitySlotDTO.setInterviewerInfoList(interviewerInfoList);

		availabilitySlotDTO.getInterviewerInfoList().sort(Comparator.comparing(InterviewerInfoDTO::getInterviewerName));
		
		return availabilitySlotDTO;
	}

	private boolean isTimeIsEquals(DayOfWeek candidateDayOfWeek, DayOfWeek interviewerDayOfWeek,
			LocalTime candidateStartTime, LocalTime interviewerStartTime,
			LocalTime candidateEndTime, LocalTime interviewerEndTime) {

		return candidateDayOfWeek.equals(interviewerDayOfWeek) && candidateStartTime.equals(interviewerStartTime) && candidateEndTime.equals(interviewerEndTime);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<String> handleResourceNotFoundException(NoSuchElementException ex) {
		return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Element not found");
	}
	
}

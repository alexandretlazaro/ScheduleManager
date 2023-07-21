package com.schedulemanager.packages.api;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO;
import com.schedulemanager.packages.dto.AvailabilitySlotDTO.InterviewerInfo;
import com.schedulemanager.packages.service.AvailabilitySlotService;
import com.schedulemanager.packages.service.CandidateService;
import com.schedulemanager.packages.service.InterviewerService;

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
	public AvailabilitySlotDTO getAllAvailabilitySlots(
			@RequestParam Long candidateId,
			@RequestParam List<Long> interviewerIds) {

		AvailabilitySlotDTO availabilitySlotDTO = new AvailabilitySlotDTO();

		List<AvailabilitySlot> matchingInterviewerAvailabilitySlots = new ArrayList<>();

		Optional<Candidate> candidate = candidateService.getCandidateById(candidateId);
		
		List<AvailabilitySlot> candidateAvailabilitySlots = candidate.get().getInterviewSlotList();

		List<Interviewer> interviewerIdsList = interviewerService.getInterviewersByIds(interviewerIds);

		List<LocalTime> candidateStartTimeList = candidate.get().getInterviewSlotList().stream()
				.map(AvailabilitySlot::getStartTime).collect(Collectors.toList());

		List<LocalTime> candidateEndTimeList = candidate.get().getInterviewSlotList().stream()
				.map(AvailabilitySlot::getEndTime).collect(Collectors.toList());

		List<DayOfWeek> candidateDayOfWeekList = candidate.get().getInterviewSlotList().stream()
				.map(AvailabilitySlot::getDayOfWeek).collect(Collectors.toList());

		for (Interviewer i : interviewerIdsList) {

			matchingInterviewerAvailabilitySlots = availabilitySlotService.findByInterviewerAndTime(i, candidateStartTimeList, candidateEndTimeList, candidateDayOfWeekList);

			List<AvailabilitySlot> interviewerAvailabilitySlots = i.getAvailabilitySlotList();

			for (AvailabilitySlot candidateSlot : candidateAvailabilitySlots) {
				
				for (AvailabilitySlot interviewerSlot : interviewerAvailabilitySlots) {
					
					if (candidateSlot.getDayOfWeek() == interviewerSlot.getDayOfWeek()) {
						
						if (isTimeOverlap(candidateSlot.getStartTime(), candidateSlot.getEndTime(),
								interviewerSlot.getStartTime(), interviewerSlot.getEndTime())) {
							
							matchingInterviewerAvailabilitySlots.add(interviewerSlot);
						}
					}
				}
			}
		}

		availabilitySlotDTO.setCandidateName(candidate.get().getName());

		Set<InterviewerInfo> interviewerInfoList = new TreeSet<>(Comparator.comparing(InterviewerInfo::getStartTime));

		for (AvailabilitySlot interviewerAvailabilitySlot : matchingInterviewerAvailabilitySlots) {

			InterviewerInfo interviewerInfo = new InterviewerInfo();

			interviewerInfo.setInterviewerName(interviewerAvailabilitySlot.getInterviewer().getName());
			interviewerInfo.setDayOfWeek(interviewerAvailabilitySlot.getDayOfWeek());
			interviewerInfo.setStartTime(interviewerAvailabilitySlot.getStartTime());
			interviewerInfo.setEndTime(interviewerAvailabilitySlot.getEndTime());

			interviewerInfoList.add(interviewerInfo);
		}

		availabilitySlotDTO.setInterviewerInfoList(new TreeSet<>(interviewerInfoList));

		return availabilitySlotDTO;
	}
	
	private boolean isTimeOverlap(LocalTime candidateStartTime, LocalTime candidateEndTime,
			LocalTime interviewerStartTime, LocalTime interviewerEndTime) {
        
		return !candidateStartTime.isAfter(interviewerEndTime) && !candidateEndTime.isBefore(interviewerStartTime);
    }

}

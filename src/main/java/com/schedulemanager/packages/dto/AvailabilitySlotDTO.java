package com.schedulemanager.packages.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.modelmapper.ModelMapper;

import com.schedulemanager.packages.domain.AvailabilitySlot;

import lombok.Data;

@Data
public class AvailabilitySlotDTO {

	private String candidateName;
	private DayOfWeek dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;
	private Set<InterviewerInfo> interviewerInfoList;
	

	public static AvailabilitySlotDTO create(AvailabilitySlot availabilitySlot) {

		ModelMapper modelMapper = new ModelMapper();

		return modelMapper.map(availabilitySlot, AvailabilitySlotDTO.class);
	}

	@Data
	public static class InterviewerInfo implements Comparable<InterviewerInfo> {

		private DayOfWeek dayOfWeek;
		private String interviewerName;
		private LocalTime startTime;
		private LocalTime endTime;
		 private List<AvailabilitySlot> availabilitySlotList = new ArrayList<>();
		
		@Override
        public int compareTo(InterviewerInfo other) {
            return this.startTime.compareTo(other.startTime);
        }

	}
	
	@Data
	public static class CandidateInfo {

		private String candidateName;

	}
}

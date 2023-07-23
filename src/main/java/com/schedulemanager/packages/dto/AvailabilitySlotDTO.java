package com.schedulemanager.packages.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.modelmapper.ModelMapper;

import com.schedulemanager.packages.domain.AvailabilitySlot;

import lombok.Data;

@Data
public class AvailabilitySlotDTO {

	private List<CandidateInfoDTO> candidateInfoList;
	private DayOfWeek dayOfWeek;
	private LocalTime startTime;
	private LocalTime endTime;
	private List<InterviewerInfoDTO> interviewerInfoList;


	public static AvailabilitySlotDTO create(AvailabilitySlot availabilitySlot) {

		ModelMapper modelMapper = new ModelMapper();

		return modelMapper.map(availabilitySlot, AvailabilitySlotDTO.class);
	}

	@Data
	public static class InterviewerInfoDTO {

		private DayOfWeek dayOfWeek;
		private String interviewerName;
		private LocalTime startTime;
		private LocalTime endTime;

	}

	@Data
	public static class CandidateInfoDTO {

		private String candidateName;
		private DayOfWeek dayOfWeek;
		private String interviewerName;
		private LocalTime startTime;
		private LocalTime endTime;

	}
}

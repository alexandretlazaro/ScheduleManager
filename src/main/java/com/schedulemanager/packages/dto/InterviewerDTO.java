package com.schedulemanager.packages.dto;

import java.util.List;

import org.modelmapper.ModelMapper;

import com.schedulemanager.packages.domain.Interviewer;

import lombok.Data;

@Data
public class InterviewerDTO {

	private Long id;
    private String name;
    private List<AvailabilitySlotDTO> availabilitySlotDTOs;
    
    public static InterviewerDTO create(Interviewer interviewer) {

		ModelMapper modelMapper = new ModelMapper();

		return modelMapper.map(interviewer, InterviewerDTO.class);
	}
}

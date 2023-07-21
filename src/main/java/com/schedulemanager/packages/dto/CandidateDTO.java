package com.schedulemanager.packages.dto;

import java.util.List;

import org.modelmapper.ModelMapper;

import com.schedulemanager.packages.domain.Candidate;

import lombok.Data;

@Data
public class CandidateDTO {

	private Long id;
	private String name;
	private List<AvailabilitySlotDTO> availabilitySlotDTOs;

	public static CandidateDTO create(Candidate candidate) {

		ModelMapper modelMapper = new ModelMapper();

		return modelMapper.map(candidate, CandidateDTO.class);
	}

}
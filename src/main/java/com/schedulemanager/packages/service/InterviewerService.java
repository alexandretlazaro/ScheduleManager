package com.schedulemanager.packages.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.repository.InterviewerRepository;

@Service
public class InterviewerService {

	@Autowired
	private InterviewerRepository repository;

	public List<Interviewer> getInterviewers() {
		
		return repository.findAll();
	}

	public Optional<Interviewer> getInterviewerById(Long id) {
		
		return repository.findById(id);
		
	}
	
	public Interviewer save(Interviewer interviewer) {

		Assert.isNull(interviewer.getId(), "It was not possible save the interviewer!");
		
		return repository.save(interviewer);
	}

	public List<Interviewer> getInterviewersByIds(List<Long> interviewerIds) {
		return repository.findAllById(interviewerIds);
	}
	
}

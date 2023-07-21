package com.schedulemanager.packages.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.repository.CandidateRepository;

@Service
public class CandidateService {

	@Autowired
	private CandidateRepository repository;

	public List<Candidate> getCandidates() {
		
		return repository.findAll();
	}

	public Optional<Candidate> getCandidateById(Long id) {
		
		return repository.findById(id);
		
	}
	
	public Candidate save(Candidate candidate) {

		Assert.isNull(candidate.getId(), "It was not possible save the candidate!");
		
		return repository.save(candidate);
	}

}

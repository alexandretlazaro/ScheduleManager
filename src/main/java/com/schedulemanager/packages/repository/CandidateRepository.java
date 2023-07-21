package com.schedulemanager.packages.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedulemanager.packages.domain.Candidate;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {

}

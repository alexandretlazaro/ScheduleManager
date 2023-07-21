package com.schedulemanager.packages.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.schedulemanager.packages.domain.Interviewer;

public interface InterviewerRepository extends JpaRepository<Interviewer, Long> {

}

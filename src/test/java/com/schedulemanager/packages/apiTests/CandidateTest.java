package com.schedulemanager.packages.apiTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.dto.CandidateDTO;
import com.schedulemanager.packages.repository.CandidateRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class CandidateTest {

	@Autowired
	private RestTemplate rest;
	
	@Autowired
	private CandidateRepository repository;

	private ResponseEntity<CandidateDTO> getCandidate(String url) {

		return rest.getForEntity(url, CandidateDTO.class);
	}

	@Test
	private ResponseEntity<List<CandidateDTO>> getCandidates(String url) {

		return rest.exchange(
				url, 
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<CandidateDTO>>() {
				});
	}

	@Test
	public void testGetAllCandidates() {

		List<CandidateDTO> candidates = getCandidates("http://localhost:8080/api/v1/schedule/candidate/").getBody();
		assertNotNull(candidates);
		assertNotEquals(true, candidates.isEmpty());
	}

	@Test
	public void testSaveCandidates() {

		Candidate candidate = new Candidate();

		candidate.setId(1L);
		candidate.setName("Alexandre");

		ResponseEntity<CandidateDTO> response = rest.postForEntity("http://localhost:8080/api/v1/schedule/candidate", candidate, null);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		String location = response.getHeaders().get("location").get(0);

		CandidateDTO candidateDTO = getCandidate(location).getBody();

		assertNotNull(candidateDTO);
		assertEquals("Alexandre", candidateDTO.getName());

	}
	
	@Test
	public void testDeleteCandidate() {
		
		Candidate candidate = new Candidate();

		candidate.setName("Alexandre");
		
		repository.save(candidate);

		Long candidateId = candidate.getId();
		
		assertTrue(repository.existsById(candidateId));
		
		repository.deleteById(candidateId);
		
		assertFalse(repository.existsById(candidateId));
		
	}
	
	@Test
	public void testSaveAvailabilitySlotCandidate() {
		
		Candidate candidate = new Candidate();
		
		candidate.setId(1L);
		candidate.setName("Alexandre");
		
		LocalTime startTime = LocalTime.parse("08:00");
		LocalTime endTime = LocalTime.parse("12:00");
		
		boolean isValid = isTimeValid(startTime, endTime);
		
		assertEquals(true, isValid);
		
		LocalTime slot1Hour = startTime;

		while(slot1Hour.isBefore(endTime)) {

			AvailabilitySlot slot = new AvailabilitySlot();

			LocalTime currentStart = slot1Hour;
			LocalTime currentEndTime = currentStart.plusHours(1);

			slot.setCandidate(candidate);
			slot.setDayOfWeek(DayOfWeek.MONDAY);
			slot.setStartTime(currentStart);
			slot.setEndTime(currentEndTime);

			candidate.getInterviewSlotList().add(slot);

			slot1Hour = currentEndTime;
		}
		
		ResponseEntity<CandidateDTO> response = rest.postForEntity("http://localhost:8080/api/v1/schedule/candidate", candidate, null);
		
		String location = response.getHeaders().get("location").get(0);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		assertNotNull(candidate.getInterviewSlotList());
		
		assertEquals(4, candidate.getInterviewSlotList().size());
		
		rest.delete(location);
		
	}

	private boolean isTimeValid(LocalTime startTime, LocalTime endTime) {
		
		boolean isValid = true;

		if(endTime.isBefore(startTime)) {
			isValid = false;
		}
		
		if(!(startTime.getMinute() == 0 && startTime.getSecond() == 0)) {

			isValid = false;
		}

		if(!(endTime.getMinute() == 0 && endTime.getSecond() == 0)) {

			isValid = false;
		}
		
		return isValid;
	}
	
	@TestConfiguration
	static class TestConfig {

		@Bean
		RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}
}
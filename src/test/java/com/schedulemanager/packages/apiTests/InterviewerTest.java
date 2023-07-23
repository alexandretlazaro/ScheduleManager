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
import com.schedulemanager.packages.domain.Interviewer;
import com.schedulemanager.packages.dto.InterviewerDTO;
import com.schedulemanager.packages.repository.InterviewerRepository;

@SpringBootTest
@AutoConfigureMockMvc
public class InterviewerTest {

	@Autowired
	private RestTemplate rest;
	
	@Autowired
	private InterviewerRepository repository;

	private ResponseEntity<InterviewerDTO> getInterviewer(String url) {

		return rest.getForEntity(url, InterviewerDTO.class);
	}

	@Test
	private ResponseEntity<List<InterviewerDTO>> getInterviewers(String url) {

		return rest.exchange(
				url, 
				HttpMethod.GET,
				null,
				new ParameterizedTypeReference<List<InterviewerDTO>>() {
				});
	}

	@Test
	public void testGetAllInterviewers() {

		List<InterviewerDTO> interviewers = getInterviewers("http://localhost:8080/api/v1/schedule/interviewer/").getBody();
		assertNotNull(interviewers);
		assertNotEquals(true, interviewers.isEmpty());
	}

	@Test
	public void testSaveInterviewer() {

		Interviewer interviewer = new Interviewer();

		interviewer.setId(1L);
		interviewer.setName("Alexandre");

		ResponseEntity<InterviewerDTO> response = rest.postForEntity("http://localhost:8080/api/v1/schedule/interviewer", interviewer, null);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());

		String location = response.getHeaders().get("location").get(0);

		InterviewerDTO interviewerDTO = getInterviewer(location).getBody();

		assertNotNull(interviewerDTO);
		assertEquals("Alexandre", interviewerDTO.getName());

	}
	
	@Test
	public void testDeleteInterviewer() {
		
		Interviewer interviewer = new Interviewer();

		interviewer.setName("Alexandre");
		
		repository.save(interviewer);

		Long interviewerId = interviewer.getId();
		
		assertTrue(repository.existsById(interviewerId));
		
		repository.deleteById(interviewerId);
		
		assertFalse(repository.existsById(interviewerId));
		
	}
	
	@Test
	public void testSaveAvailabilitySlotInterviewer() {
		
		Interviewer interviewer = new Interviewer();
		
		interviewer.setId(1L);
		interviewer.setName("Alexandre");
		
		LocalTime startTime = LocalTime.parse("08:00");
		LocalTime endTime = LocalTime.parse("12:00");
		
		boolean isValid = isTimeValid(startTime, endTime);
		
		assertEquals(true, isValid);
		
		LocalTime slot1Hour = startTime;

		while(slot1Hour.isBefore(endTime)) {

			AvailabilitySlot slot = new AvailabilitySlot();

			LocalTime currentStart = slot1Hour;
			LocalTime currentEndTime = currentStart.plusHours(1);

			slot.setInterviewer(interviewer);
			slot.setDayOfWeek(DayOfWeek.MONDAY);
			slot.setStartTime(currentStart);
			slot.setEndTime(currentEndTime);

			interviewer.getAvailabilitySlotList().add(slot);

			slot1Hour = currentEndTime;
		}
		
		ResponseEntity<InterviewerDTO> response = rest.postForEntity("http://localhost:8080/api/v1/schedule/interviewer", interviewer, null);
		
		String location = response.getHeaders().get("location").get(0);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		
		assertNotNull(interviewer.getAvailabilitySlotList());
		
		assertEquals(4, interviewer.getAvailabilitySlotList().size());
		
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
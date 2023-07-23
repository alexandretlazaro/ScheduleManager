package com.schedulemanager.packages.apiTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import com.schedulemanager.packages.domain.AvailabilitySlot;
import com.schedulemanager.packages.domain.Candidate;
import com.schedulemanager.packages.domain.Interviewer;

import jakarta.transaction.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class InterviewTest {

	@Test
	public void testGetAllInterviewsByInterviewersAndCandidate() {

		AvailabilitySlot availabilitySlot = new AvailabilitySlot();

		Candidate candidate = new Candidate();
		candidate.setName("Candidate 1");

		List<Interviewer> interviewersList = new ArrayList<>();
		List<AvailabilitySlot> candidateSlotList = new ArrayList<>();

		candidateSlotList.add(new AvailabilitySlot(1L, null, candidate, DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("09:00")));
		candidateSlotList.add(new AvailabilitySlot(2L, null, candidate, DayOfWeek.MONDAY, LocalTime.parse("09:00"), LocalTime.parse("10:00")));
		candidateSlotList.add(new AvailabilitySlot(3L, null, candidate, DayOfWeek.MONDAY, LocalTime.parse("10:00"), LocalTime.parse("11:00")));
		candidateSlotList.add(new AvailabilitySlot(4L, null, candidate, DayOfWeek.MONDAY, LocalTime.parse("11:00"), LocalTime.parse("12:00")));
		candidateSlotList.add(new AvailabilitySlot(5L, null, candidate, DayOfWeek.MONDAY, LocalTime.parse("12:00"), LocalTime.parse("13:00")));

		candidate.setInterviewSlotList(candidateSlotList);

		availabilitySlot.setCandidate(candidate);

		List<AvailabilitySlot> interviewSlotList = availabilitySlot.getCandidate().getInterviewSlotList();
		assertEquals(5, interviewSlotList.size());

		Interviewer interviewer1 = new Interviewer();
		interviewer1.setName("Interviewer 1");

		List<AvailabilitySlot> interviewerSlotList1 = new ArrayList<>();

		interviewerSlotList1.add(new AvailabilitySlot(6L, interviewer1, null, DayOfWeek.MONDAY, LocalTime.parse("08:00"), LocalTime.parse("09:00")));
		interviewerSlotList1.add(new AvailabilitySlot(7L, interviewer1, null, DayOfWeek.MONDAY, LocalTime.parse("09:00"), LocalTime.parse("10:00")));
		interviewerSlotList1.add(new AvailabilitySlot(8L, interviewer1, null, DayOfWeek.MONDAY, LocalTime.parse("10:00"), LocalTime.parse("11:00")));
		interviewerSlotList1.add(new AvailabilitySlot(9L, interviewer1, null, DayOfWeek.TUESDAY, LocalTime.parse("12:00"), LocalTime.parse("13:00")));

		interviewer1.setAvailabilitySlotList(interviewerSlotList1);

		Interviewer interviewer2 = new Interviewer();

		interviewer2.setName("Interviewer 2");

		List<AvailabilitySlot> interviewerSlotList2 = new ArrayList<>();

		interviewerSlotList2.add(new AvailabilitySlot(10L, interviewer2, null, DayOfWeek.MONDAY, LocalTime.parse("14:00"), LocalTime.parse("15:00")));

		interviewer2.setAvailabilitySlotList(interviewerSlotList2);

		Interviewer interviewer3 = new Interviewer();
		interviewer3.setName("Interviewer 3");

		List<AvailabilitySlot> interviewerSlotList3 = new ArrayList<>();

		interviewerSlotList3.add(new AvailabilitySlot(11L, interviewer3, null, DayOfWeek.WEDNESDAY, LocalTime.parse("11:00"), LocalTime.parse("12:00")));

		interviewer3.setAvailabilitySlotList(interviewerSlotList3);

		interviewersList.add(interviewer1);
		interviewersList.add(interviewer2);
		interviewersList.add(interviewer3);

			assertEquals(true, availabilitySlot.getCandidate().getInterviewSlotList().get(0).getDayOfWeek().equals(interviewer1.getAvailabilitySlotList().get(0).getDayOfWeek()));
			
			assertEquals(true, availabilitySlot.getCandidate().getInterviewSlotList().get(2).getStartTime().equals(interviewer1.getAvailabilitySlotList().get(2).getStartTime()) &&
					availabilitySlot.getCandidate().getInterviewSlotList().get(2).getEndTime().equals(interviewer1.getAvailabilitySlotList().get(2).getEndTime()));
			
			assertNotEquals(true, availabilitySlot.getCandidate().getInterviewSlotList().get(0).getStartTime().equals(interviewer2.getAvailabilitySlotList().get(0).getStartTime()) &&
					availabilitySlot.getCandidate().getInterviewSlotList().get(0).getEndTime().equals(interviewer2.getAvailabilitySlotList().get(0).getEndTime()));
			
			assertTrue(interviewer3.getName().equals("Interviewer 3"));
			
			assertFalse(availabilitySlot.getCandidate().getInterviewSlotList().get(3).getDayOfWeek().equals(interviewer3.getAvailabilitySlotList().get(0).getDayOfWeek()));

	}

	@TestConfiguration
	static class TestConfig {

		@Bean
		RestTemplate restTemplate() {
			return new RestTemplate();
		}
	}
}

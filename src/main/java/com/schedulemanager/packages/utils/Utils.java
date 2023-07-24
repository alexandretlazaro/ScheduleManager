package com.schedulemanager.packages.utils;

import java.net.URI;
import java.time.LocalTime;

import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.schedulemanager.packages.domain.AvailabilitySlot;

public abstract class Utils {

	public static boolean localTimeValidate(AvailabilitySlot availabilitySlot) {

		boolean isValid = true;

		LocalTime startTime = availabilitySlot.getStartTime();
		LocalTime endTime = availabilitySlot.getEndTime();

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
	
	public static URI getURI(Long id) {

		return ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri();
	}
}

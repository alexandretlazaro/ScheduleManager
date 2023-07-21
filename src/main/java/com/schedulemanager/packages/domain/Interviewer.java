package com.schedulemanager.packages.domain;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "interviewer")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Interviewer {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	private String name;
	
	@OneToMany(mappedBy = "interviewer", cascade = CascadeType.ALL)
	@Column(name = "inteview_slot")
    private List<AvailabilitySlot> availabilitySlotList = new ArrayList<>();
}

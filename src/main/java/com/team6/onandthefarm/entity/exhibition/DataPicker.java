package com.team6.onandthefarm.entity.exhibition;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Builder
@Slf4j
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class DataPicker {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE)
	private Long DataPickerId;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "exhibitionCategoryId")
	private ExhibitionCategory exhibitionCategory;

	private String DataPickerName;

	private boolean DataPickerUsableStatus;

	private boolean DataPickerStatus;

	private String DataPickerCreatedAt;

	private String DataPickerModifiedAt;

	private String DataPickerWriter;
}
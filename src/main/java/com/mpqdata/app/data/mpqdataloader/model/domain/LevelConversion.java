package com.mpqdata.app.data.mpqdataloader.model.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@Data
@NoArgsConstructor
@RequiredArgsConstructor
@Entity
@Table(schema = "mpq_data")
public class LevelConversion {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer levelConversionId;

	@NonNull
	private Integer rarity;

	@NonNull
	private Integer effectiveLevel;

	@NonNull
	private Integer displayLevel;

}

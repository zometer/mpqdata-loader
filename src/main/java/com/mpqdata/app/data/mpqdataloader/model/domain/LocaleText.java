package com.mpqdata.app.data.mpqdataloader.model.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;
import lombok.NonNull;

@Data
@Entity
@Table(schema = "mpq_data")
public class LocaleText {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long localeTextId;

	@NonNull
	private String textKey;

	@NonNull
	private String localeLanguage;

	@NonNull
	private String text;

}

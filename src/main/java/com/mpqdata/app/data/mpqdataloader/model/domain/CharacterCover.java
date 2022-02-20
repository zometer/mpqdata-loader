package com.mpqdata.app.data.mpqdataloader.model.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonAlias;

import lombok.Data;

@Data
@Entity
@Table(schema = "mpq_data")
public class CharacterCover {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long characterCoverId;

	private String mpqCharacterId;
	private Long marvelIssueId;
	private Long gcdIssueId;
	private Boolean customCover;
	private Boolean defaultCover;
	private String imageUrlSmall;
	private String imageUrlMedium;
	private String imageUrlLarge;
	private String series;
	private Integer seriesStartYear;
	private String issue;
	private String variant;
	private String mpqEventName;
	private Integer ordinalPosition;
	private boolean complete;

	@Transient
	@JsonAlias("AttributionLine1")
	private String attributionLine1;

	@Transient
	@JsonAlias("AttributionLine2")
	private String attributionLine2;

	@Transient
	@JsonAlias("AttributionLine3")
	private String attributionLine3;

}

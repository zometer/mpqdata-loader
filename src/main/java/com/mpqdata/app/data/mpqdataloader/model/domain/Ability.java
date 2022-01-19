package com.mpqdata.app.data.mpqdataloader.model.domain;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "mpq_data")
public class Ability {

	@Id
	private String abilityId;
	private String mpqCharacterId;
	private String nameKey;
	private String descriptionKey;
	private String color;
	private int ordinalPosition;
	private int cost;

}

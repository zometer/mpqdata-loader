package com.mpqdata.app.data.mpqdataloader.model.domain;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table(schema = "mpq_data")
public class MpqCharacter {

	@Id
	private String mpqCharacterId;
	private String nameKey;
	private String subtitleKey;
	private int rarity;
	private Date releaseDate;

}

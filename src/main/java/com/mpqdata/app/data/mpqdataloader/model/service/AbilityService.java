package com.mpqdata.app.data.mpqdataloader.model.service;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.Overrides;
import com.mpqdata.app.data.mpqdataloader.model.domain.Ability;
import com.mpqdata.app.data.mpqdataloader.model.repository.AbilityRepository;
import com.mpqdata.app.data.mpqdataloader.util.JsonPathUtils;

import lombok.Setter;

@Service
public class AbilityService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Overrides overrides;

	@Autowired
	@Setter
	private AbilityRepository abilityRepository;

	public void deleteAbilitiesByCharacterId(String mpqCharacterId) {
		abilityRepository.deleteByMpqCharacterId(mpqCharacterId);
	}

	public void loadAbilities(File file) {
		try {
			DocumentContext doc = JsonPath.parse(file);
			String charId = JsonPathUtils.extractFirstKey(doc);
			int numAbilities = doc.read("$." + charId + ".Abilities.length()", Integer.class);
			Map<String, String> abilityDescOverrides = overrides.getAbilityDescOverrides();

			for (int i=0; i < numAbilities ; i++) {
				Ability ability = new Ability();
				String color = fixColor( doc.read("$." + charId + ".Abilities[" + i + "].Color", String.class) );
				String abilityId = doc.read("$." + charId + ".Abilities[" + i + "].Type", String.class);

				ability.setMpqCharacterId(charId);
				ability.setAbilityId( abilityId );
				ability.setColor( color );
				ability.setCost(doc.read("$." + charId + ".Abilities[" + i + "].ChargeCost", Integer.class) );


				String descKey = doc.read("$." + charId + ".Abilities[" + i + "].Desc", String.class);
				ability.setDescriptionKey( overrides.replaceWithOverride(descKey, abilityDescOverrides) );
				ability.setNameKey( doc.read("$." + charId + ".Abilities[" + i + "].Name", String.class) );
				ability.setOrdinalPosition(i);

				logger.info("    [ABILITY]: " + ability);

				abilityRepository.save(ability);
			}
		} catch (IOException e) {
			throw new MpqDataLoaderException("Error parsing Ability: " + file.getAbsolutePath(), e);
		}

	}

	private String fixColor(String rawColor) {
		return "white".equalsIgnoreCase(rawColor) ? "yellow" : rawColor.toLowerCase();
	}

}

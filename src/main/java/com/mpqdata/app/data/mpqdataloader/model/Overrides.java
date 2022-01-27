package com.mpqdata.app.data.mpqdataloader.model;

import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@EnableConfigurationProperties
@ConfigurationProperties(prefix = "mpq.overrides")
@Component
public class Overrides {

	@Getter
	@Setter
	private Map<String, String> abilityDescOverrides;

	@Getter
	@Setter
	private Map<String, String> characterBioOverrides;

	public String replaceWithOverride(String key, Map<String, String> overrides) {
		if (overrides.containsKey(key)) {
			return overrides.get(key);
		}
		return key;
	}

}

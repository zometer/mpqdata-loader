package com.mpqdata.app.data.mpqdataloader.model.service;

import static com.mpqdata.app.data.mpqdataloader.model.Constants.DATE_FORMAT;
import static com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel.RARITY_1;
import static com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel.RARITY_2;
import static com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel.RARITY_3;
import static com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel.RARITY_4;
import static com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel.RARITY_5;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.Overrides;
import com.mpqdata.app.data.mpqdataloader.model.domain.MpqCharacter;
import com.mpqdata.app.data.mpqdataloader.model.repository.MpqCharacterRepository;
import com.mpqdata.app.data.mpqdataloader.util.JsonPathUtils;

import lombok.Setter;

@Service
public class MpqCharcterFileSystemService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private Overrides overrides;

	@Autowired
	@Setter
	private MpqCharacterRepository mpqCharacterRepository;

	@Autowired
	@Setter
	private AbilityService abilityService;

	public List<MpqCharacter> loadCharacterConfigs(File directory) {
		List<MpqCharacter> characters = new ArrayList<>();

		Stream.of(directory.listFiles())
			.filter(f -> ! f.getName().contains("_"))
			.filter(f -> ! f.getName().toLowerCase().contains("boss"))
			.forEach(f -> {
				logger.info("[file]: " + f.getAbsolutePath());
				MpqCharacter mpqChar = convertToMpqCharacter(f);
				if (mpqChar == null) {
					return ;
				}
				try {
					abilityService.deleteAbilitiesByCharacterId(mpqChar.getMpqCharacterId());
					mpqCharacterRepository.deleteById(mpqChar.getMpqCharacterId());
				} catch (EmptyResultDataAccessException e) {
					// Do nothing...
				}

				mpqChar = mpqCharacterRepository.save(mpqChar);
				abilityService.loadAbilities(f);
			})
		;

		return characters;
	}

	public MpqCharacter convertToMpqCharacter(File file) {
		try {
			DocumentContext doc = JsonPath.parse(file);
			if (doc.read("$.*.TimeVisibleAt", List.class).isEmpty()) {
				return null;
			}

			logger.info("    [file]: " + file.getAbsolutePath());
			String charId = JsonPathUtils.extractFirstKey(doc);
			MpqCharacter mpqChar = new MpqCharacter();
			mpqChar.setMpqCharacterId(charId);
			mpqChar.setNameKey( doc.read("$." + charId + ".Name", String.class) );
			mpqChar.setSubtitleKey( doc.read("$." + charId + ".Subtitle", String.class) );

			Map<String, String> characterBioOverrides = overrides.getCharacterBioOverrides();
			String characterBioKey = characterBioOverrides.containsKey(charId) ?
				characterBioOverrides.get(charId) :
				"Comic_Story_" + charId
			;
			mpqChar.setCharacterBioKey(characterBioKey);

			String timestamp = doc.read("$." + charId + ".TimeVisibleAt", String.class) ;
			Date releaseDate = DATE_FORMAT.parse(timestamp);
			Date now = new Date();
			if (now.before(releaseDate)) {
				return null;
			}
			mpqChar.setReleaseDate(releaseDate);

			File levelFile = new File(file.getParentFile(), mpqChar.getMpqCharacterId() + "_Levels.json");
			int rarity = extractRarityFromLevelFile(levelFile);
			mpqChar.setRarity(rarity);

			return mpqChar;
		} catch (ParseException | IOException e) {
			throw new MpqDataLoaderException("Error parsing character data", e);
		}

	}

	private int extractRarityFromLevelFile(File levelFile) throws IOException {
		List<?> effectiveLevelTemplates = JsonPath.parse(levelFile).read("$.*.TemplateEffectiveLevel", List.class);
		if (!effectiveLevelTemplates.isEmpty() ) {
			String templateValue = effectiveLevelTemplates.get(0).toString();
			logger.info("    [rarity]: effectiveLevelTemplate - " + templateValue);
			return Integer.parseInt( templateValue.replaceAll("[A-Za-z]", "") );
		}

		/* HulkBruceBanner is the only character requiring this workaround */
		Map<Integer, Integer> minLevelRarityMap = Map.of(
			RARITY_1.minLevel(), RARITY_1.rarity(),
			RARITY_2.minLevel(), RARITY_2.rarity(),
			RARITY_3.minLevel(), RARITY_3.rarity(),
			RARITY_4.minLevel(), RARITY_4.rarity(),
			RARITY_5.minLevel(), RARITY_5.rarity()
		);
		List<?> levelBaseValues = JsonPath.parse(levelFile).read("$.*.EffectiveLevel.BaseValues[0]", List.class);
		if ( ! levelBaseValues.isEmpty() ) {
			int level = Integer.parseInt( levelBaseValues.get(0).toString() );
			logger.info("    [rarity]: level - " + level);
			return minLevelRarityMap.get(level);
		}

		throw new MpqDataLoaderException("Unable to find a valid rarity based on levels: " + levelFile.getAbsolutePath());
	}

}

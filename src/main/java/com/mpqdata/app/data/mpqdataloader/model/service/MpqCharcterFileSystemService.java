package com.mpqdata.app.data.mpqdataloader.model.service;

import static com.mpqdata.app.data.mpqdataloader.model.Contants.*;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.domain.MpqCharacter;
import com.mpqdata.app.data.mpqdataloader.model.repository.MpqCharacterRepository;
import com.mpqdata.app.data.mpqdataloader.util.JsonPathUtils;

import lombok.Setter;

@Service
public class MpqCharcterFileSystemService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	@Setter
	private MpqCharacterRepository mpqCharacterRepository;

	@Autowired
	@Setter
	private AbilityService abilityService;

	@Transactional
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

			String timestamp = doc.read("$." + charId + ".TimeVisibleAt", String.class) ;
			mpqChar.setReleaseDate(DATE_FORMAT.parse(timestamp));

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
		Map<Integer, Integer> minLevelRarityMap = Map.of(1, 1, 15, 2, 40, 3, 70, 4, 255, 5);
		List<?> levelBaseValues = JsonPath.parse(levelFile).read("$.*.EffectiveLevel.BaseValues[0]", List.class);
		if ( ! levelBaseValues.isEmpty() ) {
			int level = Integer.parseInt( levelBaseValues.get(0).toString() );
			logger.info("    [rarity]: level - " + level);
			return minLevelRarityMap.get(level);
		}

		throw new MpqDataLoaderException("Unable to find a valid rarity based on levels: " + levelFile.getAbsolutePath());
	}

}

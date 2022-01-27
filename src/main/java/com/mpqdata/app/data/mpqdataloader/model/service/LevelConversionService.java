package com.mpqdata.app.data.mpqdataloader.model.service;

import static com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.JsonPath;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.domain.LevelConversion;
import com.mpqdata.app.data.mpqdataloader.model.domain.RarityLevel;
import com.mpqdata.app.data.mpqdataloader.model.repository.LevelConversionRepository;

@Service
public class LevelConversionService {

	private static final String LEVEL_DATA_FILE = "TemplateData_Levels.json";
	private static final String LEVEL_DATA_5_STAR_FILE = "TemplateData5Star_Levels.json";
	private static final int DEFAULT_LEVEL_OFFSET = 50;

	private Logger logger = LoggerFactory.getLogger(getClass());


	@Autowired
	private LevelConversionRepository levelConversionRepository;

	@Transactional
	public void loadLevelConversions(File dataDir) {
		File levelData = new File(dataDir, LEVEL_DATA_FILE);
		File levelDataRarity5 = new File(dataDir, LEVEL_DATA_5_STAR_FILE);

		loadLevelConversionsForRarity(RARITY_1, levelData, "$[3][3]", 0);
		loadLevelConversionsForRarity(RARITY_2, levelData, "$[4][3]", DEFAULT_LEVEL_OFFSET);
		loadLevelConversionsForRarity(RARITY_3, levelData, "$[5][3]", DEFAULT_LEVEL_OFFSET);
		loadLevelConversionsForRarity(RARITY_4, levelData, "$[6][3]", DEFAULT_LEVEL_OFFSET);
		loadLevelConversionsForRarity(RARITY_5, levelDataRarity5, "$.TemplateData.FloatData.EffectiveLevelRarity5", DEFAULT_LEVEL_OFFSET);
	}

	protected void loadLevelConversionsForRarity(RarityLevel rarityLevel, File file, String jsonPathExp, int offset) {
		try {
			List<LevelConversion> levelConversions = new ArrayList<>();
			logger.info("[LEVELS]: rarity - " + rarityLevel.rarity());

			@SuppressWarnings("unchecked")
			List<Integer> levels = JsonPath.parse(file).read(jsonPathExp, List.class);
			logger.info("    [LEVELS]: levels - " + levels);

			int start = rarityLevel.minLevel() - 1 + offset;

			if ( rarityLevel.maxChampLevel() == null ) {
				IntStream.range(start, rarityLevel.maxLevel() ).forEach(i -> {
					levelConversions.add( new LevelConversion(rarityLevel.rarity(), levels.get(i).intValue(), i+1) );
				});
				deleteAndSaveLevelConversions(rarityLevel, levelConversions);
				return;
			}

			// Load the regular levels (entries 0-49 are just back calculated place holders.
			IntStream.range(rarityLevel.minLevel(), rarityLevel.maxLevel() + 1 ).forEach( i -> {
				levelConversions.add( new LevelConversion(rarityLevel.rarity(), i, i) );
			});

			// Load the champion levels .
			int numChampLevels = rarityLevel.maxChampLevel() - rarityLevel.maxLevel() ;
			IntStream.range(offset, offset + numChampLevels ).forEach( i -> {
				int displayLevel = i + 1 - offset + rarityLevel.maxLevel();
				levelConversions.add( new LevelConversion(rarityLevel.rarity(), levels.get(i), displayLevel) );
			});

			deleteAndSaveLevelConversions(rarityLevel, levelConversions);
		} catch (IOException e) {
			throw new MpqDataLoaderException("Error loading level data with \"" +jsonPathExp + "\": " + file.getAbsolutePath(), e);
		}
	}

	private void deleteAndSaveLevelConversions(RarityLevel rarityLevel, List<LevelConversion> levelConversions) {
		logger.info("    [LEVELS]: delete - " + rarityLevel.rarity());
		levelConversionRepository.deleteByRarity(rarityLevel.rarity());

		logger.info("    [LEVELS]: save - " + levelConversions );
		levelConversionRepository.saveAll(levelConversions);
	}

}

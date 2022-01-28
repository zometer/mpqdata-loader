package com.mpqdata.app.data.mpqdataloader.model.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.domain.LocaleText;
import com.mpqdata.app.data.mpqdataloader.model.repository.LocaleTextRepository;

import lombok.Setter;

@Service
public class LocaleTextService {

	private static final Map<String, String> LANG_DIR_MAP = Map.of(
		"en", "English",
		"fr", "French",
		"de", "German",
		"it", "Italian",
		"ja", "Japanese",
		"ko", "Korean",
		"pt", "Portuguese",
		"ru", "Russian",
		"es", "Spanish"
	);

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Setter
	private List<String> languages;

	@Autowired
	@Setter
	private LocaleTextRepository localeTextRepository;

	public void loadLocaleText(File dataDir) {
		logger.info("[LOCALES]: start");

		logger.info("[LOCALES]: " + languages);

		List<String> keys = localeTextRepository.fetchAllTextKeys();

		logger.debug("[LOCALES]: " + keys);
		for (String language : languages) {
			loadLocaleText(language, keys, dataDir);
		}

		logger.info("[LOCALES]: complete");
	}

	protected void loadLocaleText(String language, List<String> keys, File dataDir) {
		File file = new File(dataDir, LANG_DIR_MAP.get(language) + "/locale.json" ) ;
		List<LocaleText> localeTexts = new ArrayList<>();

		try {
			DocumentContext doc = JsonPath.parse(file);
			for (String key: keys) {
				String text = doc.read( "$." + key, String.class);
				LocaleText localeText = new LocaleText(key, language, text);
				localeTexts.add(localeText);
			}

			localeTextRepository.saveAll(localeTexts);
		} catch (IOException e) {
			throw new MpqDataLoaderException(e);
		}
	}


}

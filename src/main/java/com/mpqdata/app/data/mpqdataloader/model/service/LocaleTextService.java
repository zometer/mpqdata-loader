package com.mpqdata.app.data.mpqdataloader.model.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.jayway.jsonpath.PathNotFoundException;
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

	// private Map<String, String> localeTextMap = new HashMap<>();

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
			localeTextRepository.deleteByLocaleLanguage(language);
			loadLocaleText(language, keys, dataDir);
		}

		logger.info("[LOCALES]: complete");
	}

	protected void loadLocaleText(String language, List<String> keys, File dataDir) {
		File file = new File(dataDir, LANG_DIR_MAP.get(language) + "/locale.json" ) ;
		File patchFile = new File(dataDir, LANG_DIR_MAP.get(language) + "/locale_patch.json" ) ;
		List<LocaleText> localeTexts = new ArrayList<>();
		Configuration config = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
		List<File> localeFiles = Arrays.asList(file, patchFile);

		logger.info("    [LOCALES]: loading locale files[" + language + "]: " + localeFiles);
		DocumentContext[] docs = localeFiles.stream()
			.map(f -> {
				try {
					return JsonPath.using(config).parse(f);
				} catch (IOException e) {
					throw new MpqDataLoaderException("Error parsing JSON file: " + f.getAbsolutePath(), e);
				}
			})
			.toList()
			.toArray(new DocumentContext[0])
		;
		logger.info("    [LOCALES]: loading locale files[" + language + "]: complete");

		try {
			for (String key: keys) {
				String text = readLocaleText(key, docs);
				LocaleText localeText = new LocaleText(key, language, text);
				logger.trace("    [LOCALES]: Read locale key: " + localeText);
				localeTexts.add(localeText);
			}

			localeTextRepository.saveAll(localeTexts);
		} catch (PathNotFoundException e) {

		} catch (IOException e) {
			throw new MpqDataLoaderException(e);
		}
	}

	protected String readLocaleText(String key, DocumentContext...docs) throws IOException {
		for (DocumentContext doc: docs) {
			String text = doc.read( "$." + key, String.class);
			if (text != null) {
				return text;
			}
		}

		throw new MpqDataLoaderException("Locale Key [" + key +"] not found in locale files.");
	}


}

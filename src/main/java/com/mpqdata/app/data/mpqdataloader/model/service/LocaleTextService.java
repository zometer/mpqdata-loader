package com.mpqdata.app.data.mpqdataloader.model.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

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
		File patchFile = new File(dataDir, LANG_DIR_MAP.get(language) + "/locale_patch.json" ) ;
		List<LocaleText> localeTexts = new ArrayList<>();

		try {
			for (String key: keys) {
				String text = readLocaleText(key, file, patchFile);
				LocaleText localeText = new LocaleText(key, language, text);
				localeTexts.add(localeText);
			}

			localeTextRepository.saveAll(localeTexts);
		} catch (PathNotFoundException e) {

		} catch (IOException e) {
			throw new MpqDataLoaderException(e);
		}
	}

	protected String readLocaleText(String key, File...files) throws IOException {
		Configuration config = Configuration.defaultConfiguration().addOptions(Option.SUPPRESS_EXCEPTIONS);
		for (File file: files) {
			if (! file.exists()) {
				continue;
			}
			DocumentContext doc = JsonPath.using(config).parse(file);
			String text = doc.read( "$." + key, String.class);
			if (text != null) {
				return text;
			}
		}

		List<String> fileList = Stream.of(files).map(f -> f.getAbsolutePath()).toList();
		throw new MpqDataLoaderException("Locale Key [" + key +"] not found in: " + fileList);
	}


}

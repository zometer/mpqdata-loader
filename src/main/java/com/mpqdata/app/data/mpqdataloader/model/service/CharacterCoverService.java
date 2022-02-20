package com.mpqdata.app.data.mpqdataloader.model.service;

import static java.util.stream.Collectors.toMap;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import com.jayway.jsonpath.spi.json.JacksonJsonProvider;
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.domain.CharacterCover;
import com.mpqdata.app.data.mpqdataloader.model.repository.CharacterCoverRepository;

import lombok.Setter;

@Service
public class CharacterCoverService {

	private Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	@Setter
	private CharacterCoverRepository characterCoverRepository;

	public void insertForNewCharacters() {
		characterCoverRepository.insertForNewCharacters();
	}

	public void loadCharacterCovers(File dataDir) {
		File charCoverFile = new File(dataDir, "Comics/Comics_Characters.json");
		File[] localeFiles = { new File(dataDir, "Loc/English/locale.json"), new File(dataDir, "Loc/English/locale_patch.json") } ;

		logger.debug("charCoverFile: " + charCoverFile.getAbsolutePath());

		List<CharacterCover> covers = characterCoverRepository.findByCompleteFalse();
		Map<String, CharacterCover> coverMap = covers.stream().collect(
			toMap( CharacterCover::getMpqCharacterId, c -> c )
		);

		List<CharacterCover> coversToSave = new ArrayList<>();

		try {
			Configuration config = Configuration.defaultConfiguration()
				.jsonProvider(new JacksonJsonProvider())
				.mappingProvider(new JacksonMappingProvider())
			;

			Map<String, String> localeData = loadLocaleData(config, localeFiles);

			DocumentContext doc = JsonPath.using(config).parse(charCoverFile);
			TypeRef<List<Map<String,Object>>> typeRef = new TypeRef<>() {};
			doc.read("$[?(@.length() >2)][3][?(@.Character =~ /[A-Za-z0-9]+/)]", typeRef).stream()
				.filter( map -> coverMap.containsKey( map.get("Character") ) )
				.map( map -> prepareCharacterCoverForApiQuery( coverMap.get( map.get("Character") ), map, localeData ) )
				.forEach(coversToSave::add)
			;

			characterCoverRepository.saveAll(coversToSave);
		} catch (IOException e) {
			throw new MpqDataLoaderException(e);
		}

	}

	public Map<String, String> loadLocaleData(Configuration config, File...localeFiles) {
		Map<String, String> localeText = new HashMap<String, String>();
		TypeRef<Map<String, String>> typeRef = new TypeRef<Map<String,String>>() {};

		for (File file : localeFiles) {
			try {
				JsonPath.using(config).parse(file).read("$", typeRef).entrySet().stream()
					.filter( entry -> entry.getKey().startsWith("Comic") )
					.filter( entry -> entry.getKey().contains("Name") || entry.getKey().contains("Attribution") || entry.getKey().contains("Variant") )
					.forEach( entry -> {
						System.out.println(entry);
						localeText.put(entry.getKey(), entry.getValue());
					})
				;
			} catch (IOException e) {
				throw new MpqDataLoaderException(e);
			}
		}

		return localeText;
	}

	protected CharacterCover prepareCharacterCoverForApiQuery(CharacterCover cover, Map<String, Object> comicsCoverMap, Map<String, String> localeData) {
		List<String> keys = Stream.of("AttributionLine1", "AttributionLine2", "AttributionLine3")
			.filter( k -> comicsCoverMap.containsKey(k))
			.map(k -> comicsCoverMap.get(k).toString())
			.toList()
		;
		Pattern pattern = Pattern.compile("(.+) \\((\\d{4})\\)\\s+#\\s*(\\S+).*");
		Pattern patternWithoutYear = Pattern.compile("(.+)\\s+#\\s*(\\S+).*");
		for (String key: keys) {
			String value = localeData.get(key);
			Matcher matcher = pattern.matcher(value);
			Matcher matcherWithoutYear = patternWithoutYear.matcher(value);
			if (key.startsWith("Comic_Name") && matcher.matches()) {
				System.out.println("matching: " + key + " - " + value);
				cover.setSeries( matcher.group(1) );
				cover.setSeriesStartYear( Integer.parseInt( matcher.group(2) ) );
				cover.setIssue( matcher.group(3) );
			} else if (key.startsWith("Comic_Name") && matcherWithoutYear.matches()) {
				System.out.println("matching: " + key + " - " + value);
				cover.setSeries( matcherWithoutYear.group(1) );
				cover.setIssue( matcherWithoutYear.group(2) );
			} else if (key.startsWith("Comic_Name") && ! value.matches(".*#\\s*\\d+")) {
				cover.setSeries( value );
				cover.setCustomCover(true);
			}

			if (key.startsWith("Comic_Variant")) {
				cover.setVariant( localeData.get(key) );
			}
		}

		boolean isCustom = (cover.getSeries() == null || cover.getIssue() == null);
		cover.setCustomCover( isCustom );

		return cover;
	}

}

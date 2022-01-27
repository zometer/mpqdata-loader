package com.mpqdata.app.data.mpqdataloader.exec;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.RequestEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.jayway.jsonpath.JsonPath;
import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;
import com.mpqdata.app.data.mpqdataloader.model.repository.MpqCharacterRepository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Component
@Profile("find-effective-levels")
public class FindMissingEffectiveLevels implements CommandLineRunner {

	private static final long SLEEP_MILLIS = DateUtils.MILLIS_PER_SECOND * 10;
	private static final String DEVICE_ID_HEADER = "X-Demiurge-Device-Id";
	// private static final String ALLIANCE_LEADERBOARD_URL = "https://api.demiurgeserver0.net/v3/leaderboard/%s/alliances/neighbors/limit/20/";
	// private static final String ALLIANCE_LEADERBOARD_URL = "https://api.demiurgeserver0.net/v3/leaderboard/%s/alliances/offset/%d/limit/20/";
	private static final String ALLIANCE_INFO_URL = "https://api.demiurgeserver0.net/v3/alliance/%s/info/";
	private static final String USER_ROSTER_URL = "https://api.demiurgeserver0.net/v3/remote_player_data/%s/";
	private static final String LEADERBOARD_GUID = "94074801186246ffbd219729486884d3" ;

	private Logger logger = LoggerFactory.getLogger(getClass());
	private List<String> fiveStarCharacterIds ;

	private static final Integer[] KNOWN_EFFECTIVE_LEVELS_ARRAY = new Integer[] {
			255, 256, 257, 258, 259, 260, 261, 262, 263, 264, 265, 266, 267, 268, 269, 270, 271, 273, 274, 275,
			276, 277, 278, 279, 280, 281, 282, 283, 285, 286, 288, 290, 292, 295, 297, 299, 302, 304, 306, 309,
			311, 314, 316, 319, 321, 324, 326, 329, 331, 334, 337, 339, 342, 344, 347, 350, 353, 355, 358, 361,
			364, 367, 369, 372, 375, 378, 381, 384, 387, 390, 393, 396, 399, 403, 406, 409, 412, 415, 419, 422,
			425, 428, 432, 435, 439, 442, 445, 449, 452, 456, 459, 463, 467, 470, 474, 478, 481, 485, 489, 493,
			497, 500, 504, 508, 512, 516, 520, 524, 528, 533, 537, 541, 545, 549, 554, 558, 562, 567, 571, 576,
			580, 585, 589, 594, 598, 603, 608, 612, 617, 622, 627, 632, 637, 642, 647, 652, 657, 662, 667, 672,
			677, 683, 688, 693, 699, 704, 710, 715, 721, 726, 732, 738, 743, 749, 755, 761, 767, 773, 779, 791,
			797, 803, 809, 816, 822, 829, 835, 841, 848, 855, 861, 868, 875, 881, 888, 895, 902, 909, 916, 923,
			931, 938, 945, 952, 960, 967, 975, 982, 990, 998, 1005, 1013, 1021, 1029, 1037
	};
	private static final List<Integer> KNOWN_EFFECTIVE_LEVELS = new ArrayList<>(Stream.of(KNOWN_EFFECTIVE_LEVELS_ARRAY).toList()) ;

	@Autowired
	private MpqCharacterRepository mpqCharacterRepository;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${mpq.config.device-id}")
	private String deviceId ;

	@PostConstruct
	public void init() {
		fiveStarCharacterIds = mpqCharacterRepository.findAll().stream()
			.filter( c -> c.getRarity() == 5)
			.map(c -> c.getMpqCharacterId())
			.toList()
		;
	}

	@Override
	public void run(String... args) throws Exception {
		HttpHeaders headers = new HttpHeaders();
		headers.add(DEVICE_ID_HEADER, deviceId);

		boolean shouldRun = true;
		while (shouldRun) {
			logger.info("[RUNNING]: starting loop");

			List<String> allianceGuids = loadCurrentLeaderboardAllianceGuids(restTemplate, headers, LEADERBOARD_GUID);
			/*
			List<String> allianceGuids = new ArrayList<>();
			IntStream.range(0, 20)
				.map( i -> i * 20)
				.forEach(i -> {
					String url = String.format(ALLIANCE_LEADERBOARD_URL, LEADERBOARD_GUID, Integer.valueOf(i));
					allianceGuids.addAll( loadCurrentLeaderboardAllianceGuids(restTemplate, headers, url) );
				})
			;
			*/

			for (String allianceGuid: allianceGuids) {
				List<String> userGuids = loadAllianceUsers(restTemplate, headers, allianceGuid);
				userGuids.forEach( u -> searchForMissingLevels(restTemplate, headers, u) );
			}

			shouldRun = KNOWN_EFFECTIVE_LEVELS.size() < 195;

			logger.trace("[WAITING]: Waiting");
			Thread.sleep(SLEEP_MILLIS);
		}

	}

	@SuppressWarnings("unchecked")
	private void searchForMissingLevels(RestTemplate restTemplate, HttpHeaders headers, String userGuid) {
		String url = String.format(USER_ROSTER_URL, userGuid);
		String json = fetchJsonFromUrl(restTemplate, headers, url);

		String user = JsonPath.parse(json).read("$.player_name", String.class);
		String alliance = JsonPath.parse(json).read("$.alliance_name", String.class);

		List<Map<String, Object>> mapList = JsonPath.parse(json).read("$.roster[*].['character_identifier', 'effective_level']", List.class);

		mapList.stream()
			.map(map -> new UserCharacter(user, userGuid, alliance, map.get("character_identifier").toString().split("_")[0], (Integer) map.get("effective_level")) )
			.filter( userChar -> isMissingEffectiveLevel(userChar) )
			.forEach(c -> {
				logger.info(c.toString().replaceAll("FindMissingEffectiveLevels.", "")) ;
				KNOWN_EFFECTIVE_LEVELS.add( c.getEffectiveLevel() );
			})
		;

		logger.trace(mapList.toString());

	}

	private boolean isMissingEffectiveLevel(UserCharacter userChar) {
		if ( ! fiveStarCharacterIds.contains( userChar.getCharacterId() ) ) {
			return false;
		}

		if (userChar.getEffectiveLevel() < 255 || userChar.getEffectiveLevel() > 1037) {
			return false;
		}
		return ! KNOWN_EFFECTIVE_LEVELS.contains( userChar.getEffectiveLevel() ) ;
	}

	private List<String> loadAllianceUsers(RestTemplate restTemplate, HttpHeaders headers, String allianceGuid) {
		String url = String.format(ALLIANCE_INFO_URL, allianceGuid);
		String json = fetchJsonFromUrl(restTemplate, headers, url);
		@SuppressWarnings("unchecked")
		List<String> guids = JsonPath.parse(json).read("$.alliance_members[*].guid", List.class).stream().map(g -> g.toString()).toList();

		return guids;
	}

	@SuppressWarnings("unchecked")
	private List<String> loadCurrentLeaderboardAllianceGuids(RestTemplate restTemplate, HttpHeaders headers, String url) {
		try {
			List<String> guids = new ArrayList<>();
			// String url = String.format(ALLIANCE_LEADERBOARD_URL, leaderboardGuid);
			String json = fetchJsonFromUrl(restTemplate, headers, url);

			guids = JsonPath.parse(json).read("$.alliances[*].guid", List.class).stream().map(g -> g.toString()).toList();

			logger.debug("    [LEADERBOARD] guids:  " + guids);
			return guids ;
		} catch (RestClientException e) {
			throw new MpqDataLoaderException(e);
		}
	}

	private String fetchJsonFromUrl(RestTemplate restTemplate, HttpHeaders headers, String url) {
		try {
			RequestEntity<Void> request = RequestEntity
				.get(new URI(url))
				.headers(headers)
				.build()
			;

			String json = restTemplate.exchange(request, String.class).getBody();
			return json;
		} catch (RestClientException | URISyntaxException e) {
			throw new MpqDataLoaderException(e);
		}
	}

	@Data
	@NoArgsConstructor
	@AllArgsConstructor
	private class UserCharacter {

		private String user ;
		private String userGuid ;
		private String alliance;
		private String characterId;
		private int effectiveLevel;

	}

}

package com.mpqdata.app.data.mpqdataloader.model.service;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.mpqdata.app.data.mpqdataloader.MpqDataLoaderException;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Service
@Data
public class SarUrlLookupService {

	public static final String BUILD_VERSION_HEADER = "x-demiurge-build-version";
	public static final String BUILD_CHANGELIST_HEADER = "x-demiurge-build-changelist";
	public static final String CLIENT_PLATFORM_HEADER = "x-demiurge-client-platform";
	public static final String PLATFORM_IOS = "IOS";
	public static final String CONFIG_SAR_URL_KEY = "config_url";

	@Getter(AccessLevel.NONE)
	@Setter(AccessLevel.NONE)
	private Logger logger = LoggerFactory.getLogger(getClass());

	@Setter
	private String appStoreUrl;

	@Setter
	private String appStoreVersionHtmlSelector;

	@Setter
	private String mpqUpdateSarLookupUrl;

	@Autowired
	private RestTemplate restTemplate;

	public String retrieveConfigSarUrl() {
		String mpqVersion = retrieveMpqVersionFromAppStore(appStoreUrl);
		logger.info("MPQ Version: " + mpqVersion);

		HttpHeaders headers = createHttpHeaders(mpqVersion);
		RequestEntity<String> request = newRequestEntity(headers, mpqUpdateSarLookupUrl);
		ParameterizedTypeReference<Map<String, String>> reference = new ParameterizedTypeReference<>() {};

		ResponseEntity<Map<String, String>> response = restTemplate.exchange(request, reference);
		String configSarUrl = response.getBody().get(CONFIG_SAR_URL_KEY);

		return configSarUrl;
	}

	public String retrieveMpqVersionFromAppStore(String url) {
		ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
		String html = response.getBody();
		Document document = Jsoup.parse(html);

		Element element = document.select(appStoreVersionHtmlSelector).first();

		String text = element.text();
		String version = text.replaceAll("Version", "").trim();
		return version;
	}

	public HttpHeaders createHttpHeaders(String mpqVersion) {
		String major = mpqVersion.split("\\.")[0];
		String minor = mpqVersion.split("\\.")[1];
		String patch = mpqVersion.split("\\.")[2];

		HttpHeaders headers = new HttpHeaders();
		headers.add(BUILD_VERSION_HEADER, major + "." + minor);
		headers.add(CLIENT_PLATFORM_HEADER, PLATFORM_IOS);
		headers.add(BUILD_CHANGELIST_HEADER, "CL" + patch);

		return headers;
	}

	protected RequestEntity<String> newRequestEntity(HttpHeaders httpHeaders, String urlString) {
		try {
			URI uri = new URI(urlString);

			RequestEntity<String> request = new RequestEntity<String>(httpHeaders, HttpMethod.GET, uri);
			return request;
		} catch (URISyntaxException e) {
			throw new MpqDataLoaderException("Error parsing urlString", e);
		}
	}

}

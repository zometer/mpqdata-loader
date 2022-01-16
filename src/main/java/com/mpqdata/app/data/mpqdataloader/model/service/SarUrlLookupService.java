package com.mpqdata.app.data.mpqdataloader.model.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import lombok.Data;

@Service
@Data
public class SarUrlLookupService {
	
	@Value("${mpq.appstore.url}")
	private String appStoreUrl; 
	
	@Value("${mpq.appstore.version-html-element-selector}")
	private String appStoreVersionHtmlSelector; 
	
	private String mpqVersion; 
	
	private RestTemplate restTemplate = new RestTemplate(); 
	
	public String retrieveConfigSarUrl() { 
		return null; 
	}

}

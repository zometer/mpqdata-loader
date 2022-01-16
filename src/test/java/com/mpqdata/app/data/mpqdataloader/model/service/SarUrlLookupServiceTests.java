package com.mpqdata.app.data.mpqdataloader.model.service;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.mpqdata.app.data.mpqdataloader.test.junit.ClassNameDisplayNameGenerator;

@DisplayNameGeneration(ClassNameDisplayNameGenerator.class)
@ExtendWith(MockitoExtension.class)
class SarUrlLookupServiceTests {
	
	private static final String APP_STORE_URL = "http://appstore.url/mpq";
	private static final String SELECTOR = "p.whats-new__latest__version";
	private static final String MPQ_VERSION = "244.0.591162"; 
	private static final String EXPECTED_CONFIG_SAR_URL = "https://cdn.url/path/to/config.sar";
	
	@Mock
	RestTemplate restTemplate;
	
	@Nested
	class RetrieveConfigSarUrl { 
		
		@Test
		void lookupsMpqVersionFromAppStoreUrl() {
			SarUrlLookupService service = new SarUrlLookupService(); 
			service.setRestTemplate(restTemplate);			
			service.setAppStoreUrl(APP_STORE_URL);
			service.setAppStoreVersionHtmlSelector(SELECTOR);
			
			String url = service.retrieveConfigSarUrl(); 
			
			assertNotNull(url, "Null URL returned");
			assertEquals(EXPECTED_CONFIG_SAR_URL, url, "mismatched config sar URL"); 
		}
		
	}


}
